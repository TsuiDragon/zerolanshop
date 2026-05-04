package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.OrderTemplateCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateFieldRequest;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateFieldResponse;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateResponse;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.OrderTemplate;
import cn.zerolan.zerolanshop.domain.entity.OrderTemplateField;
import cn.zerolan.zerolanshop.mapper.OrderTemplateFieldMapper;
import cn.zerolan.zerolanshop.mapper.OrderTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public interface OrderTemplateService {

    void seedDefaultTemplates();

    List<OrderTemplateResponse> list(String name, Integer status);

    OrderTemplateResponse detail(Long id);

    OrderTemplateResponse create(OrderTemplateCreateRequest request);

    OrderTemplateResponse update(Long id, OrderTemplateUpdateRequest request);

    OrderTemplateResponse updateStatus(Long id, OrderTemplateStatusRequest request);

    void delete(Long id);

    OrderTemplate getExistingTemplate(Long id);

}
