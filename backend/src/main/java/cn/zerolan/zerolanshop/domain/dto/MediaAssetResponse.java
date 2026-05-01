package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.MediaAsset;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MediaAssetResponse {
    private Long id;
    private String scene;
    private String url;
    private String filename;
    private String originalName;
    private String contentType;
    private String extension;
    private Long size;
    private Integer width;
    private Integer height;
    private String storagePath;
    private Integer status;
    private Boolean used;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MediaAssetResponse from(MediaAsset asset, boolean used) {
        MediaAssetResponse response = new MediaAssetResponse();
        response.setId(asset.getId());
        response.setScene(asset.getScene());
        response.setUrl(asset.getUrl());
        response.setFilename(asset.getFilename());
        response.setOriginalName(asset.getOriginalName());
        response.setContentType(asset.getContentType());
        response.setExtension(asset.getExtension());
        response.setSize(asset.getSize());
        response.setWidth(asset.getWidth());
        response.setHeight(asset.getHeight());
        response.setStoragePath(asset.getStoragePath());
        response.setStatus(asset.getStatus());
        response.setUsed(used);
        response.setCreateTime(asset.getCreateTime());
        response.setUpdateTime(asset.getUpdateTime());
        return response;
    }
}
