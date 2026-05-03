package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplyChannelResponse {
    private Long id;
    private String channelType;
    private String channelTypeName;
    private String name;
    private String apiUrl;
    private String userId;
    private String secretKey;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static SupplyChannelResponse from(SupplyChannel channel) {
        SupplyChannelResponse response = new SupplyChannelResponse();
        response.setId(channel.getId());
        response.setChannelType(channel.getChannelType());
        response.setChannelTypeName("YOUKAYUN".equals(channel.getChannelType()) ? "优卡云" : channel.getChannelType());
        response.setName(channel.getName());
        response.setApiUrl(channel.getApiUrl());
        response.setUserId(channel.getUserId());
        response.setSecretKey(maskSecret(channel.getSecretKey()));
        response.setSort(channel.getSort());
        response.setStatus(channel.getStatus());
        response.setCreateTime(channel.getCreateTime());
        response.setUpdateTime(channel.getUpdateTime());
        return response;
    }

    private static String maskSecret(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            return "";
        }
        if (secretKey.length() <= 6) {
            return "******";
        }
        return secretKey.substring(0, 3) + "******" + secretKey.substring(secretKey.length() - 3);
    }
}
