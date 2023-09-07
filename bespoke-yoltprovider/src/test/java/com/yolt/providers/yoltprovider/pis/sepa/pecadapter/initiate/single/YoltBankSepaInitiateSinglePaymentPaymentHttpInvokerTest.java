package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.yoltprovider.pis.sepa.YoltBankSepaPaymentHttpService;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.YoltBankSepaInitiatePaymentPreExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaInitiateSinglePaymentPaymentHttpInvokerTest {

    @InjectMocks
    private YoltBankSepaInitiateSinglePaymentPaymentHttpInvoker paymentHttpInvoker;

    @Mock
    private YoltBankSepaPaymentHttpService httpService;

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequest() {
        // given
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(new byte[0]);
        YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult = new YoltBankSepaInitiatePaymentPreExecutionResult(
                null,
                null,
                null,
                null,
                null
        );
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(httpService.postInitiateSinglePaymentRequest(httpEntity))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = paymentHttpInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(result).extracting(HttpEntity::getBody)
                .isEqualTo(fakeResponseBody);
    }
}
