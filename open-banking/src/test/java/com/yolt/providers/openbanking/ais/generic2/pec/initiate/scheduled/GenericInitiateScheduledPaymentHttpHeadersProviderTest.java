package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsent4;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericInitiateScheduledPaymentHttpHeadersProviderTest {

    @InjectMocks
    private GenericInitiateScheduledPaymentHttpHeadersProvider subject;

    @Mock
    private PaymentHttpHeadersFactory paymentHttpHeadersFactory;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnProperHttpHeadersWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticScheduledConsent4 obWriteDomesticScheduledConsent4 = new OBWriteDomesticScheduledConsent4();
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult = preparePreExecutionResult(authMeans);
        HttpHeaders expectedHttpHeaders = new HttpHeaders();

        given(paymentHttpHeadersFactory.createPaymentHttpHeaders("fakeAccessToken", authMeans, signer, obWriteDomesticScheduledConsent4))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, obWriteDomesticScheduledConsent4);

        // then
        assertThat(result).isEqualTo(expectedHttpHeaders);
    }

    private GenericInitiateScheduledPaymentPreExecutionResult preparePreExecutionResult(DefaultAuthMeans authMeans) {
        GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult = new GenericInitiateScheduledPaymentPreExecutionResult();
        preExecutionResult.setAccessToken("fakeAccessToken");
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setSigner(signer);
        return preExecutionResult;
    }
}