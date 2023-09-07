package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
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
class GenericSubmitPaymentHttpHeadersProviderTest {

    @InjectMocks
    private GenericSubmitPaymentHttpHeadersProvider subject;

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
        OBWriteDomestic2 obWriteDomestic2 = new OBWriteDomestic2();
        HttpHeaders expectedHttpHeaders = new HttpHeaders();

        given(httpHeadersFactory.createPaymentHttpHeaders(anyString(), any(DefaultAuthMeans.class), any(Signer.class), any()))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, obWriteDomestic2);

        // then
        then(httpHeadersFactory)
                .should()
                .createPaymentHttpHeaders("accessToken", authMeans, signer, obWriteDomestic2);

        assertThat(result).isEqualTo(expectedHttpHeaders);
    }
}