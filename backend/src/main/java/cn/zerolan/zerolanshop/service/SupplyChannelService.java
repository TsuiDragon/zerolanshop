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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class SupplyChannelService {

    public static final String TYPE_YOUKAYUN = "YOUKAYUN";

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;

    private final SupplyChannelMapper supplyChannelMapper;
    private final YoukayunClient youkayunClient;

    public SupplyChannelService(SupplyChannelMapper supplyChannelMapper, YoukayunClient youkayunClient) {
        this.supplyChannelMapper = supplyChannelMapper;
        this.youkayunClient = youkayunClient;
    }

    public List<SupplyChannelResponse> list(String name, String channelType, Integer status) {
        QueryWrapper<SupplyChannel> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        if (StringUtils.hasText(channelType)) {
            wrapper.eq("channel_type", normalizeChannelType(channelType));
        }
        if (status != null) {
            validateStatus(status);
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("sort", "id");
        return supplyChannelMapper.selectList(wrapper).stream().map(SupplyChannelResponse::from).toList();
    }

    public SupplyChannelResponse detail(Long id) {
        return SupplyChannelResponse.from(getExistingChannel(id));
    }

    public SupplyChannelResponse create(SupplyChannelCreateRequest request) {
        if (request == null) {
            throw new RuntimeException("Supply channel request is required");
        }
        SupplyChannel channel = new SupplyChannel();
        channel.setChannelType(normalizeChannelType(request.getChannelType()));
        channel.setName(normalizeName(request.getName()));
        channel.setApiUrl(normalizeApiUrl(request.getApiUrl()));
        channel.setUserId(normalizeRequiredText(request.getUserId(), "Supply channel user ID is required", 100));
        channel.setSecretKey(normalizeRequiredText(request.getSecretKey(), "Supply channel secret key is required", 255));
        validateSort(request.getSort());
        Integer status = request.getStatus() == null ? STATUS_ENABLED : request.getStatus();
        validateStatus(status);
        channel.setSort(request.getSort() == null ? 0 : request.getSort());
        channel.setStatus(status);
        supplyChannelMapper.insert(channel);
        return SupplyChannelResponse.from(channel);
    }

    public SupplyChannelResponse update(Long id, SupplyChannelUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("Supply channel request is required");
        }
        SupplyChannel channel = getExistingChannel(id);
        channel.setChannelType(normalizeChannelType(request.getChannelType()));
        channel.setName(normalizeName(request.getName()));
        channel.setApiUrl(normalizeApiUrl(request.getApiUrl()));
        channel.setUserId(normalizeRequiredText(request.getUserId(), "Supply channel user ID is required", 100));
        if (StringUtils.hasText(request.getSecretKey())) {
            channel.setSecretKey(normalizeRequiredText(request.getSecretKey(), "Supply channel secret key is required", 255));
        }
        validateSort(request.getSort());
        Integer status = request.getStatus() == null ? channel.getStatus() : request.getStatus();
        validateStatus(status);
        channel.setSort(request.getSort() == null ? 0 : request.getSort());
        channel.setStatus(status);
        supplyChannelMapper.updateById(channel);
        return SupplyChannelResponse.from(channel);
    }

    public SupplyChannelResponse updateStatus(Long id, SupplyChannelStatusRequest request) {
        if (request == null) {
            throw new RuntimeException("Supply channel status request is required");
        }
        SupplyChannel channel = getExistingChannel(id);
        validateStatus(request.getStatus());
        channel.setStatus(request.getStatus());
        supplyChannelMapper.updateById(channel);
        return SupplyChannelResponse.from(channel);
    }

    public void delete(Long id) {
        getExistingChannel(id);
        supplyChannelMapper.deleteById(id);
    }

    public SupplyChannelBalanceResponse balance(Long id) {
        SupplyChannel channel = getExistingChannel(id);
        if (!TYPE_YOUKAYUN.equals(channel.getChannelType())) {
            throw new RuntimeException("Unsupported supply channel type");
        }
        return youkayunClient.queryBalance(channel);
    }

    public SupplyChannelProductResponse channelProduct(Long id, String channelProductId) {
        SupplyChannel channel = getExistingChannel(id);
        if (!TYPE_YOUKAYUN.equals(channel.getChannelType())) {
            throw new RuntimeException("Unsupported supply channel type");
        }
        String normalizedProductId = normalizeRequiredText(channelProductId, "Supply channel product ID is required", 100);
        return youkayunClient.queryProduct(channel, normalizedProductId);
    }

    public SupplyChannel getExistingChannel(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Supply channel ID is required");
        }
        SupplyChannel channel = supplyChannelMapper.selectById(id);
        if (channel == null) {
            throw new RuntimeException("Supply channel does not exist");
        }
        return channel;
    }

    private String normalizeChannelType(String channelType) {
        String normalized = normalizeText(channelType);
        normalized = normalized == null ? "" : normalized.toUpperCase(Locale.ROOT);
        if (!TYPE_YOUKAYUN.equals(normalized)) {
            throw new RuntimeException("Supply channel type must be YOUKAYUN");
        }
        return normalized;
    }

    private String normalizeName(String name) {
        return normalizeRequiredText(name, "Supply channel name is required", 50);
    }

    private String normalizeApiUrl(String apiUrl) {
        String normalized = normalizeRequiredText(apiUrl, "Supply channel API URL is required", 500);
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            throw new RuntimeException("Supply channel API URL must start with http:// or https://");
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String normalizeRequiredText(String value, String message, int maxLength) {
        String normalized = normalizeText(value);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException(message);
        }
        if (normalized.length() > maxLength) {
            throw new RuntimeException(message.replace(" is required", "") + " must be " + maxLength + " characters or fewer");
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
