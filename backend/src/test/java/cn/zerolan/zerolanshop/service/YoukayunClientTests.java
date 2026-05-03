package cn.zerolan.zerolanshop.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YoukayunClientTests {

    @Test
    void buildSignUsesSortedFormParamsAndUserKey() {
        YoukayunClient client = new YoukayunClient();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("userid", "12098");

        String sign = client.buildSign(params, "8cd4167638970adfaac8ab782e41cff4");

        assertThat(sign).isEqualTo("8ef7c5f7527ea438aa26be5515740352");
    }
}
