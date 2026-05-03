package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.SupplyOrderDispatchRequest;
import cn.zerolan.zerolanshop.domain.dto.SupplyOrderDispatchResponse;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import cn.zerolan.zerolanshop.mapper.ProductSupplyBindingMapper;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SupplyOrderDispatchService {

    private static final int STATUS_ENABLED = 1;

    private final ProductMapper productMapper;
    private final ProductSupplyBindingMapper productSupplyBindingMapper;
    private final SupplyChannelMapper supplyChannelMapper;
    private final YoukayunClient youkayunClient;

    public SupplyOrderDispatchService(
            ProductMapper productMapper,
            ProductSupplyBindingMapper productSupplyBindingMapper,
            SupplyChannelMapper supplyChannelMapper,
            YoukayunClient youkayunClient
    ) {
        this.productMapper = productMapper;
        this.productSupplyBindingMapper = productSupplyBindingMapper;
        this.supplyChannelMapper = supplyChannelMapper;
        this.youkayunClient = youkayunClient;
    }

    public SupplyOrderDispatchResponse dispatch(SupplyOrderDispatchRequest request) {
        validateRequest(request);
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("Product does not exist");
        }
        ProductSupplyBinding binding = selectBinding(request.getProductId(), request.getBindingId());
        if (binding == null) {
            return SupplyOrderDispatchResponse.skipped("Product has no enabled supply channel binding");
        }
        SupplyChannel channel = supplyChannelMapper.selectById(binding.getChannelId());
        if (channel == null || channel.getStatus() == null || channel.getStatus() != STATUS_ENABLED) {
            return SupplyOrderDispatchResponse.skipped("Supply channel is disabled or missing");
        }
        if (SupplyChannelService.TYPE_YOUKAYUN.equals(channel.getChannelType())) {
            return youkayunClient.submitOrder(channel, binding, request.getExternalOrderNo(), request.getQuantity(), request.getOrderParams());
        }
        throw new RuntimeException("Unsupported supply channel type");
    }

    private ProductSupplyBinding selectBinding(Long productId, Long bindingId) {
        if (bindingId != null) {
            ProductSupplyBinding binding = productSupplyBindingMapper.selectById(bindingId);
            if (binding == null || !productId.equals(binding.getProductId()) || binding.getStatus() == null || binding.getStatus() != STATUS_ENABLED) {
                return null;
            }
            return binding;
        }
        QueryWrapper<ProductSupplyBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId)
                .eq("status", STATUS_ENABLED)
                .orderByAsc("sort", "id")
                .last("LIMIT 1");
        return productSupplyBindingMapper.selectOne(wrapper);
    }

    private void validateRequest(SupplyOrderDispatchRequest request) {
        if (request == null) {
            throw new RuntimeException("Supply order dispatch request is required");
        }
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new RuntimeException("Product ID is required");
        }
        if (!StringUtils.hasText(request.getExternalOrderNo())) {
            throw new RuntimeException("External order number is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Order quantity must be greater than 0");
        }
    }
}
