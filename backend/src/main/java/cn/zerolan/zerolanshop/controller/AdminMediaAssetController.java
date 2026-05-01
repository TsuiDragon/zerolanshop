package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.MediaAssetResponse;
import cn.zerolan.zerolanshop.domain.dto.MediaAssetUsageResponse;
import cn.zerolan.zerolanshop.service.LocalImageStorageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/media-assets")
public class AdminMediaAssetController {

    private final LocalImageStorageService localImageStorageService;

    public AdminMediaAssetController(LocalImageStorageService localImageStorageService) {
        this.localImageStorageService = localImageStorageService;
    }

    /**
     * GET /api/admin/media-assets
     * 查询图片素材列表，可按场景、文件名和使用状态过滤。
     */
    @GetMapping
    public Result<List<MediaAssetResponse>> list(
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) Boolean used
    ) {
        return Result.success(localImageStorageService.list(scene, filename, used));
    }

    /**
     * GET /api/admin/media-assets/{id}/usages
     * 查询素材当前被哪些业务资源引用。
     */
    @GetMapping("/{id}/usages")
    public Result<List<MediaAssetUsageResponse>> usages(@PathVariable Long id) {
        return Result.success(localImageStorageService.usages(id));
    }

    /**
     * DELETE /api/admin/media-assets/{id}
     * 删除未使用的素材，并同步删除本地磁盘文件。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        localImageStorageService.delete(id);
        return Result.success();
    }
}
