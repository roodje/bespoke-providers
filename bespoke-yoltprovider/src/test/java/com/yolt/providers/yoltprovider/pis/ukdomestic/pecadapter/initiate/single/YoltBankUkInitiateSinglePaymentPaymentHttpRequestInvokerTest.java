package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsent1;
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
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class YoltBankUkInitiateSinglePaymentPaymentHttpRequestInvokerTest {

    @InjectMocks
    private YoltBankUkInitiateSinglePaymentPaymentHttpRequestInvoker httpRequestInvoker;

    @Mock
    private YoltBankUkDomesticHttpService httpService;

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequest() {
        // given
        HttpEntity<OBWriteDomesticConsent1> httpEntity = Mockito.mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(httpService.postInitiateSinglePayment(httpEntity))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, null);

        // then
        then(httpService)
                .should()
                .postInitiateSinglePayment(httpEntity);
        assertThat(result).isEqualTo(responseEntity);
    }
}