package cn.zerolan.zerolanshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;

    public WebMvcConfig(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPath = normalizePublicPath(uploadProperties.getPublicPath());
        String imageLocation = Path.of(uploadProperties.getImageDir()).toAbsolutePath().normalize().toUri().toString();
        if (!imageLocation.endsWith("/")) {
            imageLocation = imageLocation + "/";
        }
        registry.addResourceHandler(publicPath + "**")
                .addResourceLocations(imageLocation);
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
}
