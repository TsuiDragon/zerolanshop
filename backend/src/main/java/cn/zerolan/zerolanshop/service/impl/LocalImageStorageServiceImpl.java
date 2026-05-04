package cn.zerolan.zerolanshop.service.impl;

import cn.zerolan.zerolanshop.service.*;

import cn.zerolan.zerolanshop.config.UploadProperties;
import cn.zerolan.zerolanshop.domain.dto.ImageUploadResponse;
import cn.zerolan.zerolanshop.domain.dto.MediaAssetResponse;
import cn.zerolan.zerolanshop.domain.dto.MediaAssetUsageResponse;
import cn.zerolan.zerolanshop.domain.entity.MediaAsset;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import cn.zerolan.zerolanshop.mapper.MediaAssetMapper;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import cn.zerolan.zerolanshop.mapper.ProductCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalImageStorageServiceImpl implements LocalImageStorageService {

    private static final long CATEGORY_MAX_SIZE = 1024 * 1024;
    private static final long PRODUCT_MAX_SIZE = 5 * 1024 * 1024;
    private static final Set<String> CATEGORY_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "svg");
    private static final Set<String> PRODUCT_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> RASTER_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");

    private final UploadProperties uploadProperties;
    private final MediaAssetMapper mediaAssetMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;

    public LocalImageStorageServiceImpl(
            UploadProperties uploadProperties,
            MediaAssetMapper mediaAssetMapper,
            ProductCategoryMapper productCategoryMapper,
            ProductMapper productMapper
    ) {
        this.uploadProperties = uploadProperties;
        this.mediaAssetMapper = mediaAssetMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.productMapper = productMapper;
    }

    public ImageUploadResponse upload(MultipartFile file, String scene) {
        String normalizedScene = normalizeScene(scene);
        validateFile(file, normalizedScene);

        String extension = getExtension(file.getOriginalFilename());
        LocalDate now = LocalDate.now();
        String filename = UUID.randomUUID() + "." + extension;
        Path relativePath = Path.of(normalizedScene, YEAR_FORMATTER.format(now), MONTH_FORMATTER.format(now), filename);
        Path root = Path.of(uploadProperties.getImageDir()).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new RuntimeException("上传路径无效");
        }

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException exception) {
            throw new RuntimeException("图片上传失败");
        }

        String url = normalizePublicPath(uploadProperties.getPublicPath()) + toUrlPath(relativePath);
        Integer[] dimensions = readImageDimensions(target, extension);
        MediaAsset asset = new MediaAsset();
        asset.setScene(normalizedScene);
        asset.setUrl(url);
        asset.setFilename(filename);
        asset.setOriginalName(StringUtils.cleanPath(file.getOriginalFilename() == null ? filename : file.getOriginalFilename()));
        asset.setContentType(file.getContentType());
        asset.setExtension(extension);
        asset.setSize(file.getSize());
        asset.setWidth(dimensions[0]);
        asset.setHeight(dimensions[1]);
        asset.setStoragePath(target.toString());
        asset.setStatus(1);
        mediaAssetMapper.insert(asset);

        ImageUploadResponse response = new ImageUploadResponse();
        response.setId(asset.getId());
        response.setUrl(url);
        response.setFilename(filename);
        response.setSize(file.getSize());
        response.setContentType(file.getContentType());
        return response;
    }

    public List<MediaAssetResponse> list(String scene, String filename, Boolean used) {
        QueryWrapper<MediaAsset> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(scene)) {
            wrapper.eq("scene", normalizeScene(scene));
        }
        if (StringUtils.hasText(filename)) {
            wrapper.and(query -> query
                    .like("filename", filename.trim())
                    .or()
                    .like("original_name", filename.trim()));
        }
        wrapper.orderByDesc("create_time").orderByDesc("id");
        return mediaAssetMapper.selectList(wrapper)
                .stream()
                .map(asset -> MediaAssetResponse.from(asset, isUsed(asset.getUrl())))
                .filter(asset -> used == null || asset.getUsed().equals(used))
                .toList();
    }

    public List<MediaAssetUsageResponse> usages(Long id) {
        MediaAsset asset = getExistingAsset(id);
        return findUsages(asset.getUrl());
    }

    public void delete(Long id) {
        MediaAsset asset = getExistingAsset(id);
        List<MediaAssetUsageResponse> usages = findUsages(asset.getUrl());
        if (!usages.isEmpty()) {
            throw new RuntimeException("媒体资源正在使用中");
        }
        mediaAssetMapper.deleteById(id);
        try {
            Files.deleteIfExists(Path.of(asset.getStoragePath()));
        } catch (IOException exception) {
            throw new RuntimeException("媒体资源文件删除失败");
        }
    }

    private MediaAsset getExistingAsset(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("媒体资源 ID 不能为空");
        }
        MediaAsset asset = mediaAssetMapper.selectById(id);
        if (asset == null) {
            throw new RuntimeException("媒体资源不存在");
        }
        return asset;
    }

    private boolean isUsed(String url) {
        return !findUsages(url).isEmpty();
    }

    private List<MediaAssetUsageResponse> findUsages(String url) {
        List<MediaAssetUsageResponse> usages = new ArrayList<>();
        QueryWrapper<ProductCategory> categoryWrapper = new QueryWrapper<>();
        categoryWrapper.eq("icon", url);
        for (ProductCategory category : productCategoryMapper.selectList(categoryWrapper)) {
            MediaAssetUsageResponse usage = new MediaAssetUsageResponse();
            usage.setType("category");
            usage.setId(category.getId());
            usage.setName(category.getName());
            usages.add(usage);
        }
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("image", url);
        for (Product product : productMapper.selectList(productWrapper)) {
            MediaAssetUsageResponse usage = new MediaAssetUsageResponse();
            usage.setType("product");
            usage.setId(product.getId());
            usage.setName(product.getName());
            usages.add(usage);
        }
        return usages;
    }

    private void validateFile(MultipartFile file, String scene) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择图片文件");
        }
        long maxSize = "category".equals(scene) ? CATEGORY_MAX_SIZE : PRODUCT_MAX_SIZE;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("图片文件过大");
        }

        String extension = getExtension(file.getOriginalFilename());
        Set<String> allowedExtensions = "category".equals(scene) ? CATEGORY_EXTENSIONS : PRODUCT_EXTENSIONS;
        if (!allowedExtensions.contains(extension)) {
            throw new RuntimeException("不支持的图片文件类型");
        }

        String contentType = file.getContentType();
        boolean validMimeType = RASTER_MIME_TYPES.contains(contentType) || ("category".equals(scene) && "image/svg+xml".equals(contentType));
        if (!validMimeType) {
            throw new RuntimeException("不支持的图片内容类型");
        }
    }

    private String normalizeScene(String scene) {
        String normalized = scene == null ? "" : scene.trim().toLowerCase(Locale.ROOT);
        if (!"category".equals(normalized) && !"product".equals(normalized)) {
            throw new RuntimeException("图片场景只能是分类或商品");
        }
        return normalized;
    }

    private String getExtension(String filename) {
        String cleanFilename = StringUtils.cleanPath(filename == null ? "" : filename);
        int dotIndex = cleanFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleanFilename.length() - 1) {
            throw new RuntimeException("图片文件扩展名不能为空");
        }
        return cleanFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private Integer[] readImageDimensions(Path path, String extension) {
        if ("svg".equals(extension)) {
            return new Integer[] { null, null };
        }
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                return new Integer[] { null, null };
            }
            return new Integer[] { image.getWidth(), image.getHeight() };
        } catch (IOException exception) {
            return new Integer[] { null, null };
        }
    }

    private String normalizePublicPath(String publicPath) {
        String normalized = publicPath == null || publicPath.isBlank() ? "/uploads/images/" : publicPath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }

    private String toUrlPath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
