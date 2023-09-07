package com.yolt.providers.knabgroup.common.payment.status;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.auth.SignatureData;
import com.yolt.providers.knabgroup.common.payment.DefaultCommonPaymentHttpHeadersProvider;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultStatusPaymentHttpHeadersProviderTest {

    @InjectMocks
    private DefaultStatusPaymentExecutionHttpHeadersProvider subject;

    @Mock
    private DefaultCommonPaymentHttpHeadersProvider headersProvider;

    @Test
    public void shouldReturnHttpHeaderForCorrectData() {
        // given
        StatusPaymentPreExecutionResult preExecutionResult = createPreExecutionResult();

        HttpHeaders commonHeaders = new HttpHeaders();
        given(headersProvider.provideHttpHeaders(anyString(), any(SignatureData.class), any(byte[].class), anyString()))
                .willReturn(commonHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, null);

        // then
        Assertions.assertThat(result).isEqualTo(commonHeaders);
    }

    private StatusPaymentPreExecutionResult createPreExecutionResult() {
        return new StatusPaymentPreExecutionResult(
                null,
                null,
                KnabGroupAuthenticationMeans.createKnabGroupAuthenticationMeans(SampleAuthenticationMeans.getSampleAuthenticationMeans(), "provider"),
                "accessToken",
                mock(Signer.class),
                "https://localhost.com",
                PaymentType.SINGLE
        );
    }
}