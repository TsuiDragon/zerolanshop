package cn.zerolan.zerolanshop.service;

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

public interface LocalImageStorageService {

    ImageUploadResponse upload(MultipartFile file, String scene);

    List<MediaAssetResponse> list(String scene, String filename, Boolean used);

    List<MediaAssetUsageResponse> usages(Long id);

    void delete(Long id);

}
