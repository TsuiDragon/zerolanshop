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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class OrderTemplateService {

    public static final String FIELD_PHONE = "PHONE";
    public static final String FIELD_QQ = "QQ";
    public static final String FIELD_EMAIL = "EMAIL";
    public static final String FIELD_ADDRESS = "ADDRESS";
    public static final String FIELD_TEXT = "TEXT";

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;

    private final OrderTemplateMapper orderTemplateMapper;
    private final OrderTemplateFieldMapper orderTemplateFieldMapper;

    public OrderTemplateService(
            OrderTemplateMapper orderTemplateMapper,
            OrderTemplateFieldMapper orderTemplateFieldMapper
    ) {
        this.orderTemplateMapper = orderTemplateMapper;
        this.orderTemplateFieldMapper = orderTemplateFieldMapper;
    }

    @PostConstruct
    @Transactional
    public void seedDefaultTemplates() {
        try {
            seedDefaultTemplate("手机号", 1, List.of(defaultField(FIELD_PHONE, "手机号", "请输入手机号", 1)));
            seedDefaultTemplate("QQ号", 2, List.of(defaultField(FIELD_QQ, "QQ号", "请输入QQ号", 1)));
            seedDefaultTemplate("收货地址", 3, List.of(defaultField(FIELD_ADDRESS, "收货地址", "请输入收货地址", 1)));
            seedDefaultTemplate("手机号+QQ号", 4, List.of(
                    defaultField(FIELD_PHONE, "手机号", "请输入手机号", 1),
                    defaultField(FIELD_QQ, "QQ号", "请输入QQ号", 2)
            ));
            seedDefaultTemplate("手机号+邮箱号", 5, List.of(
                    defaultField(FIELD_PHONE, "手机号", "请输入手机号", 1),
                    defaultField(FIELD_EMAIL, "邮箱号", "请输入邮箱号", 2)
            ));
        } catch (RuntimeException exception) {
            // Existing deployments may start before the new tables are migrated.
        }
    }

    public List<OrderTemplateResponse> list(String name, Integer status) {
        QueryWrapper<OrderTemplate> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        if (status != null) {
            validateStatus(status);
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("sort", "id");
        return orderTemplateMapper.selectList(wrapper)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderTemplateResponse detail(Long id) {
        return toResponse(getExistingTemplate(id));
    }

    @Transactional
    public OrderTemplateResponse create(OrderTemplateCreateRequest request) {
        if (request == null) {
            throw new RuntimeException("Order template request is required");
        }
        String name = normalizeName(request.getName());
        Integer status = request.getStatus() == null ? STATUS_ENABLED : request.getStatus();
        validateStatus(status);
        validateSort(request.getSort());
        validateSameName(null, name);

        OrderTemplate template = new OrderTemplate();
        template.setName(name);
        template.setDescription(normalizeDescription(request.getDescription()));
        template.setSort(request.getSort() == null ? 0 : request.getSort());
        template.setStatus(status);
        orderTemplateMapper.insert(template);
        replaceFields(template.getId(), request.getFields());
        return detail(template.getId());
    }

    @Transactional
    public OrderTemplateResponse update(Long id, OrderTemplateUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("Order template request is required");
        }
        OrderTemplate template = getExistingTemplate(id);
        String name = normalizeName(request.getName());
        Integer status = request.getStatus() == null ? template.getStatus() : request.getStatus();
        validateStatus(status);
        validateSort(request.getSort());
        validateSameName(id, name);

        template.setName(name);
        template.setDescription(normalizeDescription(request.getDescription()));
        template.setSort(request.getSort() == null ? 0 : request.getSort());
        template.setStatus(status);
        orderTemplateMapper.updateById(template);
        replaceFields(id, request.getFields());
        return detail(id);
    }

    public OrderTemplateResponse updateStatus(Long id, OrderTemplateStatusRequest request) {
        if (request == null) {
            throw new RuntimeException("Order template status request is required");
        }
        OrderTemplate template = getExistingTemplate(id);
        validateStatus(request.getStatus());
        template.setStatus(request.getStatus());
        orderTemplateMapper.updateById(template);
        return toResponse(template);
    }

    @Transactional
    public void delete(Long id) {
        getExistingTemplate(id);
        deleteFields(id);
        orderTemplateMapper.deleteById(id);
    }

    public OrderTemplate getExistingTemplate(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Order template ID is required");
        }
        OrderTemplate template = orderTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("Order template does not exist");
        }
        return template;
    }

    private OrderTemplateResponse toResponse(OrderTemplate template) {
        return OrderTemplateResponse.from(template, listFields(template.getId()));
    }

    private List<OrderTemplateFieldResponse> listFields(Long templateId) {
        QueryWrapper<OrderTemplateField> wrapper = new QueryWrapper<>();
        wrapper.eq("template_id", templateId).orderByAsc("sort", "id");
        return orderTemplateFieldMapper.selectList(wrapper)
                .stream()
                .map(OrderTemplateFieldResponse::from)
                .toList();
    }

    private void replaceFields(Long templateId, List<OrderTemplateFieldRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Order template must contain at least one field");
        }
        deleteFields(templateId);
        List<OrderTemplateFieldRequest> normalizedRequests = requests.stream()
                .sorted(Comparator.comparing(request -> request.getSort() == null ? 0 : request.getSort()))
                .toList();
        int index = 1;
        for (OrderTemplateFieldRequest request : normalizedRequests) {
            OrderTemplateField field = new OrderTemplateField();
            field.setTemplateId(templateId);
            field.setFieldType(normalizeFieldType(request.getFieldType()));
            field.setFieldName(normalizeFieldName(request.getFieldName()));
            field.setPlaceholder(normalizeDescription(request.getPlaceholder()));
            field.setRequired(Boolean.FALSE.equals(request.getRequired()) ? 0 : 1);
            field.setSort(request.getSort() == null ? index : request.getSort());
            orderTemplateFieldMapper.insert(field);
            index++;
        }
    }

    private void deleteFields(Long templateId) {
        QueryWrapper<OrderTemplateField> wrapper = new QueryWrapper<>();
        wrapper.eq("template_id", templateId);
        orderTemplateFieldMapper.delete(wrapper);
    }

    private void seedDefaultTemplate(String name, int sort, List<OrderTemplateFieldRequest> fields) {
        QueryWrapper<OrderTemplate> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        if (orderTemplateMapper.selectCount(wrapper) > 0) {
            return;
        }
        OrderTemplate template = new OrderTemplate();
        template.setName(name);
        template.setSort(sort);
        template.setStatus(STATUS_ENABLED);
        orderTemplateMapper.insert(template);
        replaceFields(template.getId(), fields);
    }

    private OrderTemplateFieldRequest defaultField(String type, String name, String placeholder, int sort) {
        OrderTemplateFieldRequest field = new OrderTemplateFieldRequest();
        field.setFieldType(type);
        field.setFieldName(name);
        field.setPlaceholder(placeholder);
        field.setRequired(true);
        field.setSort(sort);
        return field;
    }

    private void validateSameName(Long currentId, String name) {
        QueryWrapper<OrderTemplate> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        if (currentId != null) {
            wrapper.ne("id", currentId);
        }
        if (orderTemplateMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("Order template name already exists");
        }
    }

    private String normalizeName(String name) {
        String normalized = normalizeText(name);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException("Order template name is required");
        }
        if (normalized.length() > 50) {
            throw new RuntimeException("Order template name must be 50 characters or fewer");
        }
        return normalized;
    }

    private String normalizeFieldName(String fieldName) {
        String normalized = normalizeText(fieldName);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException("Order template field name is required");
        }
        if (normalized.length() > 50) {
            throw new RuntimeException("Order template field name must be 50 characters or fewer");
        }
        return normalized;
    }

    private String normalizeFieldType(String fieldType) {
        String normalized = normalizeText(fieldType);
        normalized = normalized == null ? "" : normalized.toUpperCase(Locale.ROOT);
        if (!List.of(FIELD_PHONE, FIELD_QQ, FIELD_EMAIL, FIELD_ADDRESS, FIELD_TEXT).contains(normalized)) {
            throw new RuntimeException("Order template field type is invalid");
        }
        return normalized;
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
