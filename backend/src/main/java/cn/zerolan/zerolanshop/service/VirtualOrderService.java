package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.SupplyOrderDispatchRequest;
import cn.zerolan.zerolanshop.domain.dto.SupplyOrderDispatchResponse;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderResponse;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderStatusRequest;
import cn.zerolan.zerolanshop.domain.entity.OrderTemplate;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.domain.entity.VirtualOrder;
import cn.zerolan.zerolanshop.mapper.ProductCategoryMapper;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import cn.zerolan.zerolanshop.mapper.ProductSupplyBindingMapper;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import cn.zerolan.zerolanshop.mapper.VirtualOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public interface VirtualOrderService {

    List<VirtualOrderResponse> create(Long userId, VirtualOrderCreateRequest request, String sourceIp);

    List<VirtualOrderResponse> listForUser( Long userId, String rechargeAccount, String productName, Long productId, String orderNo, String channelOrderNo, String status, LocalDateTime startTime, LocalDateTime endTime );

    List<VirtualOrderResponse> listForAdmin( String orderNo, String status, String productName, String rechargeAccount, String paymentMethod );

    VirtualOrderResponse updateStatus(Long id, VirtualOrderStatusRequest request);

    void handleYoukayunCallback(Map<String, String> params);

}
