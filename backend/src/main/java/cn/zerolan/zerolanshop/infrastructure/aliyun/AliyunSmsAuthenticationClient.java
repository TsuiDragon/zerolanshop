package cn.zerolan.zerolanshop.infrastructure.aliyun;

import cn.zerolan.zerolanshop.auth.service.SmsAuthenticationClient;
import cn.zerolan.zerolanshop.auth.service.SmsScene;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AliyunSmsAuthenticationClient implements SmsAuthenticationClient {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsAuthenticationClient.class);
    private static final String API_VERSION = "2017-05-25";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String templateCode;
    private final String registerTemplateCode;
    private final String loginTemplateCode;
    private final String passwordResetTemplateCode;
    private final String bindPhoneTemplateCode;
    private final String countryCode;
    private final String schemePrefix;
    private final int validMinutes;
    private final int codeLength;

    public AliyunSmsAuthenticationClient(
            ObjectMapper objectMapper,
            @Value("${zerolanshop.aliyun.sms-auth.enabled:false}") boolean enabled,
            @Value("${zerolanshop.aliyun.sms-auth.endpoint:dypnsapi.aliyuncs.com}") String endpoint,
            @Value("${zerolanshop.aliyun.sms-auth.access-key-id:}") String accessKeyId,
            @Value("${zerolanshop.aliyun.sms-auth.access-key-secret:}") String accessKeySecret,
            @Value("${zerolanshop.aliyun.sms-auth.sign-name:}") String signName,
            @Value("${zerolanshop.aliyun.sms-auth.template-code:}") String templateCode,
            @Value("${zerolanshop.aliyun.sms-auth.register-template-code:${zerolanshop.aliyun.sms-auth.template-code:}}") String registerTemplateCode,
            @Value("${zerolanshop.aliyun.sms-auth.login-template-code:${zerolanshop.aliyun.sms-auth.template-code:}}") String loginTemplateCode,
            @Value("${zerolanshop.aliyun.sms-auth.password-reset-template-code:${zerolanshop.aliyun.sms-auth.template-code:}}") String passwordResetTemplateCode,
            @Value("${zerolanshop.aliyun.sms-auth.bind-phone-template-code:${zerolanshop.aliyun.sms-auth.template-code:}}") String bindPhoneTemplateCode,
            @Value("${zerolanshop.aliyun.sms-auth.country-code:86}") String countryCode,
            @Value("${zerolanshop.aliyun.sms-auth.scheme-prefix:ZLS}") String schemePrefix,
            @Value("${zerolanshop.aliyun.sms-auth.valid-minutes:5}") int validMinutes,
            @Value("${zerolanshop.aliyun.sms-auth.code-length:6}") int codeLength
    ) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.endpoint = normalizeEndpoint(endpoint);
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
        this.registerTemplateCode = registerTemplateCode;
        this.loginTemplateCode = loginTemplateCode;
        this.passwordResetTemplateCode = passwordResetTemplateCode;
        this.bindPhoneTemplateCode = bindPhoneTemplateCode;
        this.countryCode = countryCode;
        this.schemePrefix = schemePrefix;
        this.validMinutes = validMinutes;
        this.codeLength = codeLength;
    }

    @Override
    public void sendCode(String phone, SmsScene scene) {
        validateConfig();
        Map<String, String> params = commonParams("SendSmsVerifyCode");
        params.put("PhoneNumber", phone);
        params.put("CountryCode", countryCode);
        params.put("SignName", signName);
        params.put("TemplateCode", templateCode(scene));
        params.put("SchemeName", schemeName(scene));
        params.put("CodeType", "1");
        params.put("ValidTime", String.valueOf(validMinutes * 60));
        params.put("Interval", "60");
        params.put("CodeLength", String.valueOf(codeLength));
        params.put("TemplateParam", templateParam());
        Map<String, Object> body = request(params);
        if (!isOk(body)) {
            throw new RuntimeException(errorMessage(body, "短信验证码发送失败"));
        }
    }

    @Override
    public boolean verifyCode(String phone, SmsScene scene, String code) {
        validateConfig();
        Map<String, String> params = commonParams("CheckSmsVerifyCode");
        params.put("PhoneNumber", phone);
        params.put("CountryCode", countryCode);
        params.put("SchemeName", schemeName(scene));
        params.put("VerifyCode", code);
        Map<String, Object> body = request(params);
        if (!isOk(body)) {
            throw new RuntimeException(errorMessage(body, "短信验证码校验失败"));
        }
        Object model = body.get("Model");
        if (model instanceof Map<?, ?> map) {
            Object result = map.get("VerifyResult");
            return "PASS".equalsIgnoreCase(result == null ? "" : String.valueOf(result));
        }
        return false;
    }

    private Map<String, String> commonParams(String action) {
        Map<String, String> params = new TreeMap<>();
        params.put("Action", action);
        params.put("Version", API_VERSION);
        params.put("Format", "JSON");
        params.put("AccessKeyId", accessKeyId);
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureVersion", "1.0");
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("Timestamp", TIMESTAMP_FORMATTER.format(Instant.now()));
        return params;
    }

    private Map<String, Object> request(Map<String, String> params) {
        try {
            Map<String, String> signedParams = new TreeMap<>(params);
            signedParams.put("Signature", signature(params));
            String query = signedParams.entrySet().stream()
                    .map(entry -> percentEncode(entry.getKey()) + "=" + percentEncode(entry.getValue()))
                    .collect(Collectors.joining("&"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://" + endpoint + "/?" + query))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("阿里云短信认证请求失败，HTTP 状态码：" + response.statusCode());
            }
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } catch (IOException exception) {
            throw new RuntimeException("阿里云短信认证响应解析失败");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("阿里云短信认证请求被中断");
        }
    }

    private String signature(Map<String, String> params) {
        String canonicalizedQuery = params.entrySet().stream()
                .map(entry -> percentEncode(entry.getKey()) + "=" + percentEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
        String stringToSign = "GET&%2F&" + percentEncode(canonicalizedQuery);
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new RuntimeException("阿里云短信认证请求签名失败");
        }
    }

    private String templateParam() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("code", "##code##");
        params.put("min", String.valueOf(validMinutes));
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("阿里云短信认证模板参数序列化失败");
        }
    }

    private String schemeName(SmsScene scene) {
        String prefix = StringUtils.hasText(schemePrefix) ? schemePrefix.trim() : "ZLS";
        return prefix + "_" + scene.getSchemeSuffix();
    }

    private String templateCode(SmsScene scene) {
        String selected = switch (scene) {
            case REGISTER -> registerTemplateCode;
            case LOGIN -> loginTemplateCode;
            case PASSWORD_RESET -> passwordResetTemplateCode;
            case BIND_PHONE -> bindPhoneTemplateCode;
        };
        return StringUtils.hasText(selected) ? selected.trim() : templateCode;
    }

    private boolean isOk(Map<String, Object> body) {
        Object code = body.get("Code");
        return code == null || "OK".equalsIgnoreCase(String.valueOf(code));
    }

    private String errorMessage(Map<String, Object> body, String fallback) {
        Object codeValue = body.get("Code");
        Object requestId = body.get("RequestId");
        Object message = body.get("Message");
        log.warn("阿里云短信认证失败，Code={}，Message={}，RequestId={}", codeValue, message, requestId);

        String code = codeValue == null ? "" : String.valueOf(codeValue);
        return switch (code) {
            case "MOBILE_NUMBER_ILLEGAL" -> "手机号格式错误";
            case "BUSINESS_LIMIT_CONTROL" -> "该手机号今日发送次数已达上限";
            case "FREQUENCY_FAIL" -> "验证码发送过于频繁，请稍后再试";
            case "INVALID_PARAMETERS" -> "阿里云短信参数不合法，请检查签名和模板配置";
            case "FUNCTION_NOT_OPENED" -> "阿里云号码认证服务未开通";
            case "isv.SMS_TEMPLATE_ILLEGAL" -> "阿里云短信模板不合法或未通过审核";
            case "isv.SMS_SIGNATURE_ILLEGAL" -> "阿里云短信签名不合法或未通过审核";
            case "isv.INVALID_PARAMETERS" -> "阿里云短信参数不合法";
            case "SignatureDoesNotMatch" -> "阿里云 AccessKeySecret 或签名计算不正确";
            case "InvalidAccessKeyId.NotFound" -> "阿里云 AccessKeyId 不存在或未启用";
            case "Forbidden.RAM" -> "阿里云 RAM 账号缺少短信认证接口权限";
            default -> StringUtils.hasText(code) ? fallback + "，错误码：" + code : fallback;
        };
    }

    private String percentEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    private String normalizeEndpoint(String endpoint) {
        String normalized = endpoint == null ? "" : endpoint.trim();
        normalized = normalized.replaceFirst("^https?://", "");
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return StringUtils.hasText(normalized) ? normalized : "dypnsapi.aliyuncs.com";
    }

    private void validateConfig() {
        if (!enabled) {
            throw new RuntimeException("阿里云短信认证未启用");
        }
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            throw new RuntimeException("阿里云短信认证密钥未配置");
        }
        if (!StringUtils.hasText(signName) || !StringUtils.hasText(templateCode)) {
            throw new RuntimeException("阿里云短信签名或模板未配置");
        }
    }
}
