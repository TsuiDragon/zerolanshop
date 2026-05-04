package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.ProductCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductResponse;
import cn.zerolan.zerolanshop.domain.dto.ProductStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductSupplyBindingRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductSupplyBindingResponse;
import cn.zerolan.zerolanshop.domain.dto.ProductUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.OrderTemplate;
import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.mapper.PricingTemplateMapper;
import cn.zerolan.zerolanshop.mapper.ProductCategoryMapper;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import cn.zerolan.zerolanshop.mapper.ProductSupplyBindingMapper;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public interface ProductService {

    String TYPE_VIRTUAL = "VIRTUAL";
    String TYPE_CARD = "CARD";
    String TYPE_NORMAL = "NORMAL";
    String SUPPLY_COST_LOWEST = "LOWEST";
    String SUPPLY_COST_HIGHEST = "HIGHEST";

    List<ProductResponse> list(String name, String productType, Long categoryId, Integer status);

    ProductResponse detail(Long id);

    ProductResponse create(ProductCreateRequest request);

    ProductResponse update(Long id, ProductUpdateRequest request);

    ProductResponse updateStatus(Long id, ProductStatusRequest request);

    void delete(Long id);

}
