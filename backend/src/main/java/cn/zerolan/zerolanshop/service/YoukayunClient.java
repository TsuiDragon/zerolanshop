package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.SupplyOrderDispatchResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelBalanceResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelProductResponse;
import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class YoukayunClient {

    private static final String ORDER_BUY_PATH = "/api/buygoods";
    private static final String BALANCE_PATH = "/api/getusermoney";
    private static final String PRODUCT_DETAIL_PATH = "/api/goodsdetails";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YoukayunClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), new ObjectMapper());
    }

    public YoukayunClient(ObjectMapper objectMapper) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), objectMapper);
    }

    YoukayunClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public SupplyOrderDispatchResponse submitOrder(
            SupplyChannel channel,
            ProductSupplyBinding binding,
            String externalOrderNo,
            Integer quantity,
            Map<String, Object> orderParams
    ) {
        try {
            Map<String, Object> body = buildOrderBody(binding, externalOrderNo, quantity, orderParams);
            body.put("userid", channel.getUserId());
            body.put("sign", buildSign(body, channel.getSecretKey()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildUrl(channel.getApiUrl(), ORDER_BUY_PATH)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(toFormBody(body), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Youkayun order request failed with HTTP " + response.statusCode());
            }
            return parseResponse(response.body(), externalOrderNo);
        } catch (IOException exception) {
            throw new RuntimeException("Youkayun order response parse failed");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Youkayun order request interrupted");
        }
    }

    public SupplyChannelBalanceResponse queryBalance(SupplyChannel channel) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("userid", channel.getUserId());
            body.put("sign", buildSign(body, channel.getSecretKey()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildUrl(channel.getApiUrl(), BALANCE_PATH)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(toFormBody(body), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Youkayun balance request failed with HTTP " + response.statusCode());
            }
            return parseBalanceResponse(channel, response.body());
        } catch (IOException exception) {
            throw new RuntimeException("Youkayun balance response parse failed");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Youkayun balance request interrupted");
        }
    }

    public SupplyChannelProductResponse queryProduct(SupplyChannel channel, String channelProductId) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("goodsid", channelProductId);
            body.put("userid", channel.getUserId());
            body.put("sign", buildSign(body, channel.getSecretKey()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildUrl(channel.getApiUrl(), PRODUCT_DETAIL_PATH)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(toFormBody(body), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Youkayun product request failed with HTTP " + response.statusCode());
            }
            return parseProductResponse(channel, channelProductId, response.body());
        } catch (IOException exception) {
            throw new RuntimeException("Youkayun product response parse failed");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Youkayun product request interrupted");
        }
    }

    String buildSign(Map<String, Object> params, String userKey) {
        String signText = params.entrySet().stream()
                .filter(entry -> !"sign".equals(entry.getKey()))
                .filter(entry -> entry.getValue() != null && !String.valueOf(entry.getValue()).isEmpty())
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(entry -> entry.getKey() + "=" + signValue(entry.getValue()))
                .collect(Collectors.joining("&"));
        return md5(signText + userKey);
    }

    private String toFormBody(Map<String, Object> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(signValue(entry.getValue())))
                .collect(Collectors.joining("&"));
    }

    private String signValue(Object value) {
        if (value instanceof Map<?, ?> || value instanceof Iterable<?>) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (IOException exception) {
                throw new RuntimeException("Youkayun request parameter serialization failed");
            }
        }
        return String.valueOf(value);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String buildUrl(String apiUrl, String path) {
        String normalizedApiUrl = apiUrl == null ? "" : apiUrl.trim();
        if (normalizedApiUrl.endsWith("/")) {
            normalizedApiUrl = normalizedApiUrl.substring(0, normalizedApiUrl.length() - 1);
        }
        return normalizedApiUrl + path;
    }

    Map<String, Object> buildOrderBody(
            ProductSupplyBinding binding,
            String externalOrderNo,
            Integer quantity,
            Map<String, Object> orderParams
    ) {
        Map<String, Object> params = orderParams == null ? Map.of() : orderParams;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("goodsid", binding.getChannelProductId());
        body.put("quantity", quantity);
        body.put("mark", firstPresent(params, "mark", "remark", "note"));
        body.put("maxmoney", firstPresent(params, "maxmoney", "maxMoney"));
        body.put("accountname", firstPresent(params, "accountname", "accountName", "account", "username"));
        body.put("outorderno", externalOrderNo);
        body.put("callbackurl", firstPresent(params, "callbackurl", "callbackUrl", "notifyUrl"));
        return body;
    }

    private SupplyOrderDispatchResponse parseResponse(String responseBody, String externalOrderNo) throws IOException {
        Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {});
        Object code = body.get("code");
        boolean success = code == null || "0".equals(String.valueOf(code)) || "200".equals(String.valueOf(code)) || "1000".equals(String.valueOf(code));
        if (!success) {
            Object message = body.getOrDefault("message", body.getOrDefault("msg", "Youkayun order request failed"));
            throw new RuntimeException(String.valueOf(message));
        }
        Object data = body.get("data");
        String channelOrderNo = null;
        if (data instanceof Map<?, ?> dataMap) {
            Object orderNo = firstPresent(dataMap, "ordersn", "order_no", "orderNo");
            channelOrderNo = orderNo == null ? null : String.valueOf(orderNo);
        }
        SupplyOrderDispatchResponse response = new SupplyOrderDispatchResponse();
        response.setDispatched(true);
        response.setChannelType(SupplyChannelService.TYPE_YOUKAYUN);
        response.setExternalOrderNo(externalOrderNo);
        response.setChannelOrderNo(channelOrderNo);
        response.setMessage("Youkayun order dispatched");
        return response;
    }

    private SupplyChannelBalanceResponse parseBalanceResponse(SupplyChannel channel, String responseBody) throws IOException {
        Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {});
        Object code = body.get("code");
        boolean success = code == null
                || "0".equals(String.valueOf(code))
                || "200".equals(String.valueOf(code))
                || "1000".equals(String.valueOf(code));
        if (!success) {
            Object message = body.getOrDefault("message", body.getOrDefault("msg", "Youkayun balance request failed"));
            throw new RuntimeException(String.valueOf(message));
        }
        BigDecimal balance = extractBalance(body);
        SupplyChannelBalanceResponse response = new SupplyChannelBalanceResponse();
        response.setChannelId(channel.getId());
        response.setChannelType(channel.getChannelType());
        response.setBalance(balance);
        response.setMessage("Youkayun balance queried");
        return response;
    }

    private SupplyChannelProductResponse parseProductResponse(SupplyChannel channel, String channelProductId, String responseBody) throws IOException {
        Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {});
        Object code = body.get("code");
        boolean success = code == null
                || "0".equals(String.valueOf(code))
                || "200".equals(String.valueOf(code))
                || "1000".equals(String.valueOf(code));
        if (!success) {
            Object message = body.getOrDefault("message", body.getOrDefault("msg", "Youkayun product request failed"));
            throw new RuntimeException(String.valueOf(message));
        }
        Map<?, ?> dataMap = body.get("data") instanceof Map<?, ?> map ? map : body;
        SupplyChannelProductResponse response = new SupplyChannelProductResponse();
        response.setChannelId(channel.getId());
        response.setChannelType(channel.getChannelType());
        response.setChannelProductId(channelProductId);
        response.setChannelProductName(toText(firstPresent(dataMap, "name", "goodsname", "goods_name", "title", "productName")));
        Object price = firstPresent(dataMap, "price", "money", "cost", "cost_price", "goodsprice", "goods_price", "amount");
        response.setChannelCostPrice(price == null ? null : toBigDecimal(price));
        response.setMessage("Youkayun product queried");
        return response;
    }

    private BigDecimal extractBalance(Map<String, Object> body) {
        Object direct = firstPresent(body, "balance", "money", "amount", "remain_balance", "available_balance");
        if (direct != null) {
            return toBigDecimal(direct);
        }
        Object data = body.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object nested = firstPresent(dataMap, "balance", "money", "amount", "remain_balance", "available_balance");
            if (nested != null) {
                return toBigDecimal(nested);
            }
        }
        return null;
    }

    private Object firstPresent(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : new BigDecimal(text);
    }

    private String toText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("MD5 algorithm is unavailable");
        }
    }
}
