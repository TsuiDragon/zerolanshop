package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.PricingTemplateCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateResponse;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import cn.zerolan.zerolanshop.mapper.PricingTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PricingTemplateService {

    public static final String TYPE_PERCENTAGE = "PERCENTAGE";
    public static final String TYPE_FIXED_AMOUNT = "FIXED_AMOUNT";

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;

    private final PricingTemplateMapper pricingTemplateMapper;

    public PricingTemplateService(PricingTemplateMapper pricingTemplateMapper) {
        this.pricingTemplateMapper = pricingTemplateMapper;
    }

    /**
     * 查询定价模板列表，支持按名称、定价方式和状态筛选。
     */
    public List<PricingTemplateResponse> list(String name, String pricingType, Integer status) {
        QueryWrapper<PricingTemplate> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        if (StringUtils.hasText(pricingType)) {
            wrapper.eq("pricing_type", normalizePricingType(pricingType));
        }
        if (status != null) {
            validateStatus(status);
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("sort", "id");
        return pricingTemplateMapper.selectList(wrapper)
                .stream()
                .map(PricingTemplateResponse::from)
                .toList();
    }

    public PricingTemplateResponse detail(Long id) {
        return PricingTemplateResponse.from(getExistingTemplate(id));
    }

    /**
     * 创建定价模板，模板名称在未删除数据中保持唯一。
     */
    public PricingTemplateResponse create(PricingTemplateCreateRequest request) {
        if (request == null) {
            throw new RuntimeException("Pricing template request is required");
        }
        String name = normalizeName(request.getName());
        String pricingType = normalizePricingType(request.getPricingType());
        BigDecimal pricingValue = normalizePricingValue(request.getPricingValue());
        Integer status = request.getStatus() == null ? STATUS_ENABLED : request.getStatus();
        validateStatus(status);
        validateSort(request.getSort());
        validateSameName(null, name);

        PricingTemplate template = new PricingTemplate();
        template.setName(name);
        template.setPricingType(pricingType);
        template.setPricingValue(pricingValue);
        template.setDescription(normalizeDescription(request.getDescription()));
        template.setSort(request.getSort() == null ? 0 : request.getSort());
        template.setStatus(status);
        pricingTemplateMapper.insert(template);
        return PricingTemplateResponse.from(template);
    }

    /**
     * 更新定价模板的可编辑字段。
     */
    public PricingTemplateResponse update(Long id, PricingTemplateUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("Pricing template request is required");
        }
        PricingTemplate template = getExistingTemplate(id);
        String name = normalizeName(request.getName());
        String pricingType = normalizePricingType(request.getPricingType());
        BigDecimal pricingValue = normalizePricingValue(request.getPricingValue());
        Integer status = request.getStatus() == null ? template.getStatus() : request.getStatus();
        validateStatus(status);
        validateSort(request.getSort());
        validateSameName(id, name);

        template.setName(name);
        template.setPricingType(pricingType);
        template.setPricingValue(pricingValue);
        template.setDescription(normalizeDescription(request.getDescription()));
        template.setSort(request.getSort() == null ? 0 : request.getSort());
        template.setStatus(status);
        pricingTemplateMapper.updateById(template);
        return PricingTemplateResponse.from(template);
    }

    public PricingTemplateResponse updateStatus(Long id, PricingTemplateStatusRequest request) {
        if (request == null) {
            throw new RuntimeException("Pricing template status request is required");
        }
        PricingTemplate template = getExistingTemplate(id);
        validateStatus(request.getStatus());
        template.setStatus(request.getStatus());
        pricingTemplateMapper.updateById(template);
        return PricingTemplateResponse.from(template);
    }

    public void delete(Long id) {
        getExistingTemplate(id);
        pricingTemplateMapper.deleteById(id);
    }

    /**
     * 供后续商品定价复用：成本价 + 模板规则，统一保留 2 位小数。
     */
    public BigDecimal calculateSalePrice(BigDecimal channelCostPrice, PricingTemplate template) {
        if (channelCostPrice == null || channelCostPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Channel cost price must be greater than or equal to 0");
        }
        if (template == null) {
            throw new RuntimeException("Pricing template is required");
        }
        BigDecimal pricingValue = normalizePricingValue(template.getPricingValue());
        BigDecimal salePrice;
        if (TYPE_PERCENTAGE.equals(template.getPricingType())) {
            salePrice = channelCostPrice.multiply(BigDecimal.ONE.add(pricingValue.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)));
        } else if (TYPE_FIXED_AMOUNT.equals(template.getPricingType())) {
            salePrice = channelCostPrice.add(pricingValue);
        } else {
            throw new RuntimeException("Pricing type must be PERCENTAGE or FIXED_AMOUNT");
        }
        return salePrice.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateSalePrice(BigDecimal channelCostPrice, Long templateId) {
        return calculateSalePrice(channelCostPrice, getExistingTemplate(templateId));
    }

    private PricingTemplate getExistingTemplate(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Pricing template ID is required");
        }
        PricingTemplate template = pricingTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("Pricing template does not exist");
        }
        return template;
    }

    private void validateSameName(Long currentId, String name) {
        QueryWrapper<PricingTemplate> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        if (currentId != null) {
            wrapper.ne("id", currentId);
        }
        if (pricingTemplateMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("Pricing template name already exists");
        }
    }

    private String normalizeName(String name) {
        String normalized = normalizeText(name);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException("Pricing template name is required");
        }
        if (normalized.length() > 50) {
            throw new RuntimeException("Pricing template name must be 50 characters or fewer");
        }
        return normalized;
    }

    private String normalizePricingType(String pricingType) {
        String normalized = normalizeText(pricingType);
        if (!TYPE_PERCENTAGE.equals(normalized) && !TYPE_FIXED_AMOUNT.equals(normalized)) {
            throw new RuntimeException("Pricing type must be PERCENTAGE or FIXED_AMOUNT");
        }
        return normalized;
    }

    private BigDecimal normalizePricingValue(BigDecimal pricingValue) {
        if (pricingValue == null) {
            throw new RuntimeException("Pricing value is required");
        }
        if (pricingValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Pricing value must be greater than or equal to 0");
        }
        return pricingValue;
    }

    private String normalizeDescription(String description) {
        String normalized = normalizeText(description);
        if (normalized != null && normalized.length() > 255) {
            throw new RuntimeException("Description must be 255 characters or fewer");
        }
        return normalized;
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != STATUS_DISABLED && status != STATUS_ENABLED)) {
            throw new RuntimeException("Status must be 0 or 1");
        }
    }

    private void validateSort(Integer sort) {
        if (sort != null && sort < 0) {
            throw new RuntimeException("Sort must be greater than or equal to 0");
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}
