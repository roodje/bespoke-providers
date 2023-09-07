package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsent1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class YoltBankUkInitiatePeriodicPaymentPaymentHttpRequestInvokerTest {

    @InjectMocks
    private YoltBankUkInitiatePeriodicPaymentPaymentHttpRequestInvoker httpRequestInvoker;

    @Mock
    private YoltBankUkDomesticHttpService httpService;

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequest() {
        // given
        HttpEntity<OBWriteDomesticStandingOrderConsent1> httpEntity = Mockito.mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(httpService.postInitiatePeriodicPayment(httpEntity))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, null);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }
}