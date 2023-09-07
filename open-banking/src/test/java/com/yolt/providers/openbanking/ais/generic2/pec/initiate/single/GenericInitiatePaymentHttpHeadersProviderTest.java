package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericInitiatePaymentHttpHeadersProviderTest {

    @InjectMocks
    private GenericInitiatePaymentHttpHeadersProvider subject;

    @Mock
    private PaymentHttpHeadersFactory paymentHttpHeadersFactory;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnProperHttpHeadersWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticConsent4 obWriteDomesticConsent4 = new OBWriteDomesticConsent4();
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericInitiatePaymentPreExecutionResult preExecutionResult = preparePreExecutionResult(authMeans);
        HttpHeaders expectedHttpHeaders = new HttpHeaders();

        given(paymentHttpHeadersFactory.createPaymentHttpHeaders(anyString(), any(DefaultAuthMeans.class), any(Signer.class), any()))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, obWriteDomesticConsent4);

        // then
        then(paymentHttpHeadersFactory)
                .should()
                .createPaymentHttpHeaders("fakeAccessToken", authMeans, signer, obWriteDomesticConsent4);

        assertThat(result).isEqualTo(expectedHttpHeaders);
    }

    private GenericInitiatePaymentPreExecutionResult preparePreExecutionResult(DefaultAuthMeans authMeans) {
        GenericInitiatePaymentPreExecutionResult preExecutionResult = new GenericInitiatePaymentPreExecutionResult();
        preExecutionResult.setAccessToken("fakeAccessToken");
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setSigner(signer);
        return preExecutionResult;
    }
}