package com.yolt.providers.openbanking.ais.generic2.pec.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericPaymentHttpHeadersFactoryTest {

    @InjectMocks
    private GenericPaymentHttpHeadersFactory subject;

    @Mock
    private PaymentRequestSigner paymentRequestSigner;

    @Mock
    private PaymentRequestIdempotentKeyProvider paymentRequestIdempotentKeyProvider;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnProperSetOfPaymentHttpHeadersWhenCorrectDataAreProvided() {
        // given
        String accessToken = "accessToken";
        String body = "body";
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder()
                .institutionId("institutionId")
                .build();
        String idempotentKey = UUID.randomUUID().toString();

        given(paymentRequestIdempotentKeyProvider.provideIdempotentKey())
                .willReturn(idempotentKey);
        given(paymentRequestSigner.createRequestSignature(any(), any(DefaultAuthMeans.class), any(Signer.class)))
                .willReturn("requestSignature");

        // when
        HttpHeaders result = subject.createPaymentHttpHeaders(accessToken, authMeans, signer, body);

        // then
        then(paymentRequestIdempotentKeyProvider)
                .should()
                .provideIdempotentKey();
        then(paymentRequestSigner)
                .should()
                .createRequestSignature(body, authMeans, signer);

        assertThat(result.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        HttpHeaders.AUTHORIZATION, "Bearer accessToken",
                        HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                        HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, "institutionId",
                        HttpExtraHeaders.IDEMPOTENT_KEY, idempotentKey,
                        HttpExtraHeaders.SIGNATURE_HEADER_NAME, "requestSignature"
                ));
    }

    @Test
    void shouldReturnProperSetOfCommonPaymentHttpHeadersWhenCorrectDataAreProvided() {
        // given
        String accessToken = "accessToken";
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder()
                .institutionId("institutionId")
                .build();
        String idempotentKey = UUID.randomUUID().toString();

        given(paymentRequestIdempotentKeyProvider.provideIdempotentKey())
                .willReturn(idempotentKey);

        // when
        HttpHeaders result = subject.createCommonPaymentHttpHeaders(accessToken, authMeans);

        // then
        then(paymentRequestIdempotentKeyProvider)
                .should()
                .provideIdempotentKey();

        assertThat(result.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        HttpHeaders.AUTHORIZATION, "Bearer accessToken",
                        HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                        HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, "institutionId",
                        HttpExtraHeaders.IDEMPOTENT_KEY, idempotentKey
                ));
    }
}