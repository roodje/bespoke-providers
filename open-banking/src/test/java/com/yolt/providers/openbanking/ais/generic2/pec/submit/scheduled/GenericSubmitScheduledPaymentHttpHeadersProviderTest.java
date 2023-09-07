package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericSubmitScheduledPaymentHttpHeadersProviderTest {

    @InjectMocks
    private GenericSubmitScheduledPaymentHttpHeadersProvider subject;

    @Mock
    private PaymentHttpHeadersFactory httpHeadersFactory;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnProperHttpHeadersWhenCorrectDataAreProvided() {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericSubmitPaymentPreExecutionResult preExecutionResult = new GenericSubmitPaymentPreExecutionResult();
        preExecutionResult.setAccessToken("accessToken");
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setSigner(signer);
        OBWriteDomesticScheduled2 obWriteDomestic2 = new OBWriteDomesticScheduled2();
        HttpHeaders expectedHttpHeaders = new HttpHeaders();

        given(httpHeadersFactory.createPaymentHttpHeaders("accessToken", authMeans, signer, obWriteDomestic2))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, obWriteDomestic2);

        // then
        assertThat(result).isEqualTo(expectedHttpHeaders);
    }
}