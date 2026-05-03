package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

@Data
public class SupplyOrderDispatchResponse {
    private Boolean dispatched;
    private String channelType;
    private String externalOrderNo;
    private String channelOrderNo;
    private String message;

    public static SupplyOrderDispatchResponse skipped(String message) {
        SupplyOrderDispatchResponse response = new SupplyOrderDispatchResponse();
        response.setDispatched(false);
        response.setMessage(message);
        return response;
    }
}
