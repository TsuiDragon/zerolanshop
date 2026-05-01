package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

@Data
public class ImageUploadResponse {
    private Long id;
    private String url;
    private String filename;
    private Long size;
    private String contentType;
}
