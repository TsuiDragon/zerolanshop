package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.ImageUploadResponse;
import cn.zerolan.zerolanshop.service.LocalImageStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {

    private final LocalImageStorageService localImageStorageService;

    public AdminFileController(LocalImageStorageService localImageStorageService) {
        this.localImageStorageService = localImageStorageService;
    }

    /**
     * POST /api/admin/files/images
     * 上传后台管理图片资源，返回可直接访问的本地静态资源路径。
     */
    @PostMapping("/images")
    public Result<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("scene") String scene
    ) {
        return Result.success(localImageStorageService.upload(file, scene));
    }
}
