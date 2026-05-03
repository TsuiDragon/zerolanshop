package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.SupplyChannelBalanceResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupplyChannelServiceTests {

    private final SupplyChannelMapper supplyChannelMapper = mock(SupplyChannelMapper.class);
    private final YoukayunClient youkayunClient = mock(YoukayunClient.class);
    private final SupplyChannelService supplyChannelService = new SupplyChannelService(supplyChannelMapper, youkayunClient);

    @Test
    void createMasksSecretInResponse() {
        SupplyChannelCreateRequest request = new SupplyChannelCreateRequest();
        request.setChannelType(SupplyChannelService.TYPE_YOUKAYUN);
        request.setName("Youkayun primary");
        request.setApiUrl("https://api.example.com/");
        request.setUserId("10001");
        request.setSecretKey("abcdef123456");

        SupplyChannelResponse response = supplyChannelService.create(request);

        ArgumentCaptor<SupplyChannel> channelCaptor = ArgumentCaptor.forClass(SupplyChannel.class);
        verify(supplyChannelMapper).insert(channelCaptor.capture());
        assertThat(channelCaptor.getValue().getSecretKey()).isEqualTo("abcdef123456");
        assertThat(response.getSecretKey()).isEqualTo("abc******456");
        assertThat(response.getApiUrl()).isEqualTo("https://api.example.com");
    }

    @Test
    void updateKeepsExistingSecretWhenRequestSecretIsBlank() {
        SupplyChannel existing = existingChannel();
        when(supplyChannelMapper.selectById(8L)).thenReturn(existing);

        SupplyChannelUpdateRequest request = new SupplyChannelUpdateRequest();
        request.setChannelType(SupplyChannelService.TYPE_YOUKAYUN);
        request.setName("New channel");
        request.setApiUrl("https://new.example.com");
        request.setUserId("new-user");
        request.setSecretKey("");
        request.setStatus(1);
        request.setSort(2);

        supplyChannelService.update(8L, request);

        ArgumentCaptor<SupplyChannel> channelCaptor = ArgumentCaptor.forClass(SupplyChannel.class);
        verify(supplyChannelMapper).updateById(channelCaptor.capture());
        assertThat(channelCaptor.getValue().getSecretKey()).isEqualTo("old-secret");
    }

    @Test
    void balanceDelegatesToYoukayunClient() {
        SupplyChannel existing = existingChannel();
        when(supplyChannelMapper.selectById(8L)).thenReturn(existing);
        SupplyChannelBalanceResponse clientResponse = new SupplyChannelBalanceResponse();
        clientResponse.setChannelId(8L);
        clientResponse.setChannelType(SupplyChannelService.TYPE_YOUKAYUN);
        clientResponse.setBalance(new BigDecimal("15.30"));
        when(youkayunClient.queryBalance(existing)).thenReturn(clientResponse);

        SupplyChannelBalanceResponse response = supplyChannelService.balance(8L);

        verify(youkayunClient).queryBalance(existing);
        assertThat(response.getBalance()).isEqualByComparingTo("15.30");
    }

    private SupplyChannel existingChannel() {
        SupplyChannel existing = new SupplyChannel();
        existing.setId(8L);
        existing.setChannelType(SupplyChannelService.TYPE_YOUKAYUN);
        existing.setName("Old channel");
        existing.setApiUrl("https://old.example.com");
        existing.setUserId("old-user");
        existing.setSecretKey("old-secret");
        existing.setStatus(1);
        existing.setSort(0);
        return existing;
    }
}
