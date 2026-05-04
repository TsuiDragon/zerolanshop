package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.PricingTemplateCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateResponse;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import cn.zerolan.zerolanshop.mapper.PricingTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public interface PricingTemplateService {

    String TYPE_PERCENTAGE = "PERCENTAGE";
    String TYPE_FIXED_AMOUNT = "FIXED_AMOUNT";

    List<PricingTemplateResponse> list(String name, String pricingType, Integer status);

    PricingTemplateResponse detail(Long id);

    PricingTemplateResponse create(PricingTemplateCreateRequest request);

    PricingTemplateResponse update(Long id, PricingTemplateUpdateRequest request);

    PricingTemplateResponse updateStatus(Long id, PricingTemplateStatusRequest request);

    void delete(Long id);

    BigDecimal calculateSalePrice(BigDecimal channelCostPrice, PricingTemplate template);

    BigDecimal calculateSalePrice(BigDecimal channelCostPrice, Long templateId);

}
