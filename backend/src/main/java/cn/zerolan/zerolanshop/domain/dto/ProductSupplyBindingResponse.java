package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductSupplyBindingResponse {
    private Long id;
    private Long productId;
    private Long channelId;
    private String channelName;
    private String channelType;
    private String channelProductId;
    private String channelProductName;
    private BigDecimal channelCostPrice;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ProductSupplyBindingResponse from(
            ProductSupplyBinding binding,
            String channelName,
            String channelType
    ) {
        ProductSupplyBindingResponse response = new ProductSupplyBindingResponse();
        response.setId(binding.getId());
        response.setProductId(binding.getProductId());
        response.setChannelId(binding.getChannelId());
        response.setChannelName(channelName);
        response.setChannelType(channelType);
        response.setChannelProductId(binding.getChannelProductId());
        response.setChannelProductName(binding.getChannelProductName());
        response.setChannelCostPrice(binding.getChannelCostPrice());
        response.setSort(binding.getSort());
        response.setStatus(binding.getStatus());
        response.setCreateTime(binding.getCreateTime());
        response.setUpdateTime(binding.getUpdateTime());
        return response;
    }
}
