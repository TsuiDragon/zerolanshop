package cn.zerolan.zerolanshop.service.impl;

import cn.zerolan.zerolanshop.service.*;

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
public class SupplyOrderDispatchServiceImpl implements SupplyOrderDispatchService {

    private static final int STATUS_ENABLED = 1;

    private final ProductMapper productMapper;
    private final ProductSupplyBindingMapper productSupplyBindingMapper;
    private final SupplyChannelMapper supplyChannelMapper;
    private final YoukayunClient youkayunClient;

    public SupplyOrderDispatchServiceImpl(
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
            throw new RuntimeException("商品不存在");
        }
        ProductSupplyBinding binding = selectBinding(request.getProductId(), request.getBindingId());
        if (binding == null) {
            return SupplyOrderDispatchResponse.skipped("商品没有启用的供货渠道绑定");
        }
        SupplyChannel channel = supplyChannelMapper.selectById(binding.getChannelId());
        if (channel == null || channel.getStatus() == null || channel.getStatus() != STATUS_ENABLED) {
            return SupplyOrderDispatchResponse.skipped("供货渠道不存在或已禁用");
        }
        if (SupplyChannelService.TYPE_YOUKAYUN.equals(channel.getChannelType())) {
            return youkayunClient.submitOrder(channel, binding, request.getExternalOrderNo(), request.getQuantity(), request.getOrderParams());
        }
        throw new RuntimeException("不支持的供货渠道类型");
    }

    private ProductSupplyBinding selectBinding(Long productId, Long bindingId) {
        if (bindingId != null) {
            ProductSupplyBinding binding = productSupplyBindingMapper.selectById(bindingId);
            if (binding == null || !productId.equals(binding.getProductId()) || binding.getStatus() == null || binding.getStatus() != STATUS_ENABLED || !Boolean.TRUE.equals(binding.getActive())) {
                return null;
            }
            return binding;
        }
        QueryWrapper<ProductSupplyBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId)
                .eq("status", STATUS_ENABLED)
                .eq("active", true)
                .orderByAsc("sort", "id")
                .last("LIMIT 1");
        return productSupplyBindingMapper.selectOne(wrapper);
    }

    private void validateRequest(SupplyOrderDispatchRequest request) {
        if (request == null) {
            throw new RuntimeException("供货订单分发请求不能为空");
        }
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new RuntimeException("商品 ID 不能为空");
        }
        if (!StringUtils.hasText(request.getExternalOrderNo())) {
            throw new RuntimeException("外部订单号不能为空");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("订单数量必须大于 0");
        }
    }
}
