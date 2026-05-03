package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.SupplyOrderDispatchRequest;
import cn.zerolan.zerolanshop.domain.dto.SupplyOrderDispatchResponse;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderResponse;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderStatusRequest;
import cn.zerolan.zerolanshop.domain.entity.OrderTemplate;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.domain.entity.VirtualOrder;
import cn.zerolan.zerolanshop.mapper.ProductCategoryMapper;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import cn.zerolan.zerolanshop.mapper.ProductSupplyBindingMapper;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import cn.zerolan.zerolanshop.mapper.VirtualOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class VirtualOrderService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_EXCEPTION = "EXCEPTION";
    public static final String PAYMENT_BALANCE = "BALANCE";

    private static final int ENABLED = 1;
    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final VirtualOrderMapper virtualOrderMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductSupplyBindingMapper productSupplyBindingMapper;
    private final SupplyChannelMapper supplyChannelMapper;
    private final UserMapper userMapper;
    private final OrderTemplateService orderTemplateService;
    private final SupplyOrderDispatchService supplyOrderDispatchService;
    private final ObjectMapper objectMapper;
    private final String callbackBaseUrl;
    private final String youkayunOrderCallbackPath;

    public VirtualOrderService(
            VirtualOrderMapper virtualOrderMapper,
            ProductMapper productMapper,
            ProductCategoryMapper productCategoryMapper,
            ProductSupplyBindingMapper productSupplyBindingMapper,
            SupplyChannelMapper supplyChannelMapper,
            UserMapper userMapper,
            OrderTemplateService orderTemplateService,
            SupplyOrderDispatchService supplyOrderDispatchService,
            ObjectMapper objectMapper,
            @Value("${zerolanshop.callback.base-url:http://localhost:8080}") String callbackBaseUrl,
            @Value("${zerolanshop.callback.youkayun-order-path:/api/callbacks/youkayun/order}") String youkayunOrderCallbackPath
    ) {
        this.virtualOrderMapper = virtualOrderMapper;
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.productSupplyBindingMapper = productSupplyBindingMapper;
        this.supplyChannelMapper = supplyChannelMapper;
        this.userMapper = userMapper;
        this.orderTemplateService = orderTemplateService;
        this.supplyOrderDispatchService = supplyOrderDispatchService;
        this.objectMapper = objectMapper;
        this.callbackBaseUrl = callbackBaseUrl;
        this.youkayunOrderCallbackPath = youkayunOrderCallbackPath;
    }

    @Transactional
    public List<VirtualOrderResponse> create(Long userId, VirtualOrderCreateRequest request, String sourceIp) {
        validateCreateRequest(request);
        User user = getExistingUser(userId);
        Product product = getPurchasableProduct(request.getProductId());
        validateQuantity(product, request.getQuantity());
        List<String> rechargeAccounts = parseRechargeAccounts(request.getRechargeAccount());

        BigDecimal singleAmount = product.getSalePrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = singleAmount
                .multiply(BigDecimal.valueOf(rechargeAccounts.size()))
                .setScale(2, RoundingMode.HALF_UP);
        if (user.getBalance() == null || user.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        ProductSupplyBinding activeBinding = selectActiveBinding(product.getId());
        SupplyChannel activeChannel = activeBinding == null ? null : supplyChannelMapper.selectById(activeBinding.getChannelId());
        LocalDateTime now = LocalDateTime.now();
        String productSnapshot = buildProductSnapshot(product, activeBinding, activeChannel);
        user.setBalance(user.getBalance().subtract(totalAmount).setScale(2, RoundingMode.HALF_UP));
        userMapper.updateById(user);

        List<VirtualOrder> orders = rechargeAccounts.stream().map(account -> {
            VirtualOrder order = new VirtualOrder();
            order.setOrderNo(generateOrderNo(LocalDateTime.now()));
            order.setUserId(user.getId());
            order.setUsername(user.getUsername());
            order.setProductId(product.getId());
            order.setProductName(product.getName());
            order.setProductSnapshot(productSnapshot);
            order.setQuantity(request.getQuantity());
            order.setRechargeAccount(account);
            order.setOrderAmount(singleAmount);
            order.setPaymentMethod(PAYMENT_BALANCE);
            order.setSourceIp(normalizeIp(sourceIp));
            order.setStatus(STATUS_PENDING);
            order.setCreatedAt(now);
            order.setUpdateTime(now);
            virtualOrderMapper.insert(order);
            return order;
        }).toList();

        if (activeBinding != null && activeChannel != null && activeChannel.getStatus() != null && activeChannel.getStatus() == ENABLED) {
            orders.forEach(order -> dispatch(order, activeChannel));
        }
        return orders.stream().map(VirtualOrderResponse::from).toList();
    }

    public List<VirtualOrderResponse> listForUser(
            Long userId,
            String rechargeAccount,
            String productName,
            Long productId,
            String orderNo,
            String channelOrderNo,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        QueryWrapper<VirtualOrder> wrapper = buildListWrapper(rechargeAccount, productName, productId, orderNo, channelOrderNo, status, null, startTime, endTime);
        wrapper.eq("user_id", userId);
        return virtualOrderMapper.selectList(wrapper).stream().map(VirtualOrderResponse::from).toList();
    }

    public List<VirtualOrderResponse> listForAdmin(
            String orderNo,
            String status,
            String productName,
            String rechargeAccount,
            String paymentMethod
    ) {
        QueryWrapper<VirtualOrder> wrapper = buildListWrapper(rechargeAccount, productName, null, orderNo, null, status, paymentMethod, null, null);
        return virtualOrderMapper.selectList(wrapper).stream().map(VirtualOrderResponse::from).toList();
    }

    @Transactional
    public VirtualOrderResponse updateStatus(Long id, VirtualOrderStatusRequest request) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Order ID is required");
        }
        if (request == null || !StringUtils.hasText(request.getStatus())) {
            throw new RuntimeException("Order status is required");
        }
        String status = normalizeStatus(request.getStatus());
        if (!List.of(STATUS_SUCCESS, STATUS_REFUNDED, STATUS_EXCEPTION).contains(status)) {
            throw new RuntimeException("Manual status can only be SUCCESS, REFUNDED, or EXCEPTION");
        }
        VirtualOrder order = virtualOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("Order does not exist");
        }
        markTerminal(order, status, request.getExceptionMessage());
        virtualOrderMapper.updateById(order);
        return VirtualOrderResponse.from(order);
    }

    private void dispatch(VirtualOrder order, SupplyChannel channel) {
        try {
            SupplyOrderDispatchRequest dispatchRequest = new SupplyOrderDispatchRequest();
            dispatchRequest.setProductId(order.getProductId());
            dispatchRequest.setExternalOrderNo(order.getOrderNo());
            dispatchRequest.setQuantity(order.getQuantity());
            dispatchRequest.setOrderParams(Map.of(
                    "accountname", order.getRechargeAccount(),
                    "callbackurl", buildYoukayunOrderCallbackUrl()
            ));
            SupplyOrderDispatchResponse dispatchResponse = supplyOrderDispatchService.dispatch(dispatchRequest);
            if (Boolean.TRUE.equals(dispatchResponse.getDispatched())) {
                order.setStatus(STATUS_PROCESSING);
                order.setChannelId(channel.getId());
                order.setChannelName(channel.getName());
                order.setChannelType(channel.getChannelType());
                order.setChannelOrderNo(dispatchResponse.getChannelOrderNo());
                order.setExceptionMessage(null);
                order.setUpdateTime(LocalDateTime.now());
                virtualOrderMapper.updateById(order);
            }
        } catch (RuntimeException exception) {
            markTerminal(order, STATUS_EXCEPTION, exception.getMessage());
            virtualOrderMapper.updateById(order);
        }
    }

    @Transactional
    public void handleYoukayunCallback(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("Youkayun callback params are required");
        }
        String orderNo = normalizeText(params.get("orderno"));
        String channelOrderNo = normalizeText(params.get("outorderno"));
        String callbackStatus = normalizeText(params.get("status"));
        if (!StringUtils.hasText(orderNo) && !StringUtils.hasText(channelOrderNo)) {
            throw new RuntimeException("Youkayun callback order number is required");
        }
        if (!StringUtils.hasText(callbackStatus)) {
            throw new RuntimeException("Youkayun callback status is required");
        }

        VirtualOrder order = findCallbackOrder(orderNo, channelOrderNo);
        if (order == null) {
            throw new RuntimeException("Order does not exist");
        }
        if (StringUtils.hasText(channelOrderNo)) {
            order.setChannelOrderNo(channelOrderNo);
        }
        if ("3".equals(callbackStatus)) {
            markTerminal(order, STATUS_SUCCESS, null);
        } else if ("5".equals(callbackStatus)) {
            markTerminal(order, STATUS_REFUNDED, null);
        } else {
            order.setExceptionMessage("Youkayun callback status: " + callbackStatus);
            order.setUpdateTime(LocalDateTime.now());
        }
        virtualOrderMapper.updateById(order);
    }

    private VirtualOrder findCallbackOrder(String orderNo, String channelOrderNo) {
        QueryWrapper<VirtualOrder> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.eq("order_no", orderNo);
        } else {
            wrapper.eq("channel_order_no", channelOrderNo);
        }
        wrapper.last("LIMIT 1");
        VirtualOrder order = virtualOrderMapper.selectOne(wrapper);
        if (order != null || !StringUtils.hasText(channelOrderNo)) {
            return order;
        }
        QueryWrapper<VirtualOrder> fallback = new QueryWrapper<>();
        fallback.eq("channel_order_no", channelOrderNo).last("LIMIT 1");
        return virtualOrderMapper.selectOne(fallback);
    }

    private String buildYoukayunOrderCallbackUrl() {
        String baseUrl = callbackBaseUrl == null ? "" : callbackBaseUrl.trim();
        String path = youkayunOrderCallbackPath == null || youkayunOrderCallbackPath.isBlank()
                ? "/api/callbacks/youkayun/order"
                : youkayunOrderCallbackPath.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
    }

    private QueryWrapper<VirtualOrder> buildListWrapper(
            String rechargeAccount,
            String productName,
            Long productId,
            String orderNo,
            String channelOrderNo,
            String status,
            String paymentMethod,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        QueryWrapper<VirtualOrder> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(rechargeAccount)) {
            wrapper.like("recharge_account", rechargeAccount.trim());
        }
        if (StringUtils.hasText(productName)) {
            wrapper.like("product_name", productName.trim());
        }
        if (productId != null && productId > 0) {
            wrapper.eq("product_id", productId);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like("order_no", orderNo.trim());
        }
        if (StringUtils.hasText(channelOrderNo)) {
            wrapper.like("channel_order_no", channelOrderNo.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", normalizeStatus(status));
        }
        if (StringUtils.hasText(paymentMethod)) {
            wrapper.eq("payment_method", normalizePaymentMethod(paymentMethod));
        }
        if (startTime != null) {
            wrapper.ge("created_at", startTime);
        }
        if (endTime != null) {
            wrapper.le("created_at", endTime);
        }
        wrapper.orderByDesc("created_at", "id");
        return wrapper;
    }

    private void validateCreateRequest(VirtualOrderCreateRequest request) {
        if (request == null) {
            throw new RuntimeException("Order request is required");
        }
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new RuntimeException("Product ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
        normalizePaymentMethod(request.getPaymentMethod());
        parseRechargeAccounts(request.getRechargeAccount());
    }

    private List<String> parseRechargeAccounts(String rechargeAccount) {
        String normalized = normalizeRequired(rechargeAccount, "Recharge account is required", 5000);
        List<String> accounts = Arrays.stream(normalized.split("\\R"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        if (accounts.isEmpty()) {
            throw new RuntimeException("Recharge account is required");
        }
        if (accounts.size() > 200) {
            throw new RuntimeException("Recharge account count must be 200 or fewer");
        }
        return accounts;
    }

    private Product getPurchasableProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("Product does not exist");
        }
        if (product.getStatus() == null || product.getStatus() != ENABLED) {
            throw new RuntimeException("Product is disabled");
        }
        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != ENABLED) {
            throw new RuntimeException("Product category is disabled");
        }
        return product;
    }

    private User getExistingUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("User ID is required");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("Account is disabled");
        }
        return user;
    }

    private void validateQuantity(Product product, Integer quantity) {
        int min = product.getMinPurchaseQuantity() == null ? 1 : product.getMinPurchaseQuantity();
        if (quantity < min) {
            throw new RuntimeException("Quantity must be greater than or equal to minimum purchase quantity");
        }
        if (product.getMaxPurchaseQuantity() != null && quantity > product.getMaxPurchaseQuantity()) {
            throw new RuntimeException("Quantity must be less than or equal to maximum purchase quantity");
        }
    }

    private ProductSupplyBinding selectActiveBinding(Long productId) {
        QueryWrapper<ProductSupplyBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId)
                .eq("status", ENABLED)
                .eq("active", true)
                .orderByAsc("sort", "id")
                .last("LIMIT 1");
        return productSupplyBindingMapper.selectOne(wrapper);
    }

    private String buildProductSnapshot(Product product, ProductSupplyBinding binding, SupplyChannel channel) {
        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        OrderTemplate orderTemplate = orderTemplateService.getExistingTemplate(product.getOrderTemplateId());
        Map<String, Object> snapshot = Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "productType", product.getProductType(),
                "categoryId", product.getCategoryId(),
                "categoryName", category == null ? "" : category.getName(),
                "image", product.getImage() == null ? "" : product.getImage(),
                "faceValue", product.getFaceValue() == null ? "" : product.getFaceValue(),
                "salePrice", product.getSalePrice(),
                "orderTemplateName", orderTemplate.getName(),
                "supplyBinding", bindingSnapshot(binding, channel)
        );
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Product snapshot serialization failed");
        }
    }

    private Map<String, Object> bindingSnapshot(ProductSupplyBinding binding, SupplyChannel channel) {
        if (binding == null) {
            return Map.of();
        }
        return Map.of(
                "bindingId", binding.getId(),
                "channelId", binding.getChannelId(),
                "channelName", channel == null ? "" : channel.getName(),
                "channelType", channel == null ? "" : channel.getChannelType(),
                "channelProductId", binding.getChannelProductId(),
                "channelProductName", binding.getChannelProductName(),
                "channelCostPrice", binding.getChannelCostPrice()
        );
    }

    private void markTerminal(VirtualOrder order, String status, String exceptionMessage) {
        LocalDateTime processedAt = LocalDateTime.now();
        order.setStatus(status);
        order.setProcessedAt(processedAt);
        order.setProcessingDurationSeconds(Duration.between(order.getCreatedAt(), processedAt).getSeconds());
        order.setExceptionMessage(StringUtils.hasText(exceptionMessage) ? exceptionMessage.trim() : null);
        order.setUpdateTime(processedAt);
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!List.of(STATUS_PENDING, STATUS_PROCESSING, STATUS_SUCCESS, STATUS_REFUNDED, STATUS_EXCEPTION).contains(normalized)) {
            throw new RuntimeException("Unsupported order status");
        }
        return normalized;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String normalized = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!PAYMENT_BALANCE.equals(normalized)) {
            throw new RuntimeException("Only BALANCE payment is supported");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException(message);
        }
        if (normalized.length() > maxLength) {
            throw new RuntimeException(message.replace(" is required", "") + " must be " + maxLength + " characters or fewer");
        }
        return normalized;
    }

    private String normalizeIp(String sourceIp) {
        String normalized = sourceIp == null ? "" : sourceIp.trim();
        return normalized.length() > 50 ? normalized.substring(0, 50) : normalized;
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String generateOrderNo(LocalDateTime now) {
        return now.format(ORDER_NO_TIME) + ThreadLocalRandom.current().nextInt(100000, 999999);
    }
}
