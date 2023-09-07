package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.auth.KnabSigningService;
import com.yolt.providers.knabgroup.common.auth.SignatureData;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.knabgroup.common.payment.DefaultCommonPaymentHttpHeadersProvider.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(MockitoExtension.class)
class DefaultCommonPaymentHttpHeadersProviderTest {

    private DefaultCommonPaymentHttpHeadersProvider subject;

    @Mock
    private Signer signer;

    @Mock
    private KnabSigningService knabSigningService;

    @Test
    void shouldProvideCommonHeadersForCorrectInputInInitPaymentFlow() {
        // given
        Map<String, BasicAuthenticationMean> authMeansMap = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        KnabGroupAuthenticationMeans authMeans = KnabGroupAuthenticationMeans.createKnabGroupAuthenticationMeans(authMeansMap, null);
        String base64Certificate = authMeans.getSigningData(signer).getSigningCertificateInBase64();
        subject = new DefaultCommonPaymentHttpHeadersProvider(ExternalTracingUtil::createLastExternalTraceId, knabSigningService);
        given(knabSigningService.calculateSignature(
                any(HttpHeaders.class),
                any(SignatureData.class),
                any(List.class)
        )).willReturn("testSignature");
        given(knabSigningService.calculateDigest(any())).willReturn("testDigest");

        // when
        HttpHeaders result = subject.provideHttpHeaders("testAccessToken", authMeans.getSigningData(signer), new byte[0], "fakePsuIpAddress");

        // then
        assertThat(result.getFirst(X_REQUEST_ID_HEADER_NAME)).isNotNull();
        assertThat(result).extracting(
                HttpHeaders::getContentType,
                h -> h.getFirst(HttpHeaders.AUTHORIZATION),
                h -> h.getFirst(SIGNATURE_HEADER_NAME),
                h -> h.getFirst(DIGEST_HEADER_NAME),
                h -> h.getFirst(TPP_SIGNATURE_CERTIFICATE),
                h -> h.getFirst(PSU_IP_ADDRESS_HEADER_NAME)
        ).contains(
                APPLICATION_JSON,
                "Bearer testAccessToken",
                "testSignature",
                "testDigest",
                base64Certificate,
                "fakePsuIpAddress"
        );
    }
}
