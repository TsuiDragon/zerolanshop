package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

@Data
public class SupplyChannelCreateRequest {
    private String channelType;
    private String name;
    private String apiUrl;
    private String userId;
    private String secretKey;
    private Integer sort;
    private Integer status;
}
