package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
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

    @Test
    void buildOrderBodyUsesBuyGoodsApiDocumentFields() {
        YoukayunClient client = new YoukayunClient();
        ProductSupplyBinding binding = new ProductSupplyBinding();
        binding.setChannelProductId("10086");
        Map<String, Object> orderParams = new LinkedHashMap<>();
        orderParams.put("accountname", "test-account");
        orderParams.put("mark", "test-mark");
        orderParams.put("maxmoney", "9.99");
        orderParams.put("callbackurl", "https://example.com/callback");

        Map<String, Object> body = client.buildOrderBody(binding, "LOCAL202605030001", 2, orderParams);

        assertThat(body).containsEntry("goodsid", "10086");
        assertThat(body).containsEntry("quantity", 2);
        assertThat(body).containsEntry("accountname", "test-account");
        assertThat(body).containsEntry("mark", "test-mark");
        assertThat(body).containsEntry("maxmoney", "9.99");
        assertThat(body).containsEntry("outorderno", "LOCAL202605030001");
        assertThat(body).containsEntry("callbackurl", "https://example.com/callback");
        assertThat(body).doesNotContainKeys("id", "out_trade_no", "attach");
    }
}
