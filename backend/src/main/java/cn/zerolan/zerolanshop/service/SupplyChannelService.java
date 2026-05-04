package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.SupplyChannelCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelBalanceResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelProductResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Locale;

public interface SupplyChannelService {

    String TYPE_YOUKAYUN = "YOUKAYUN";

    List<SupplyChannelResponse> list(String name, String channelType, Integer status);

    SupplyChannelResponse detail(Long id);

    SupplyChannelResponse create(SupplyChannelCreateRequest request);

    SupplyChannelResponse update(Long id, SupplyChannelUpdateRequest request);

    SupplyChannelResponse updateStatus(Long id, SupplyChannelStatusRequest request);

    void delete(Long id);

    SupplyChannelBalanceResponse balance(Long id);

    SupplyChannelProductResponse channelProduct(Long id, String channelProductId);

    SupplyChannel getExistingChannel(Long id);

}
