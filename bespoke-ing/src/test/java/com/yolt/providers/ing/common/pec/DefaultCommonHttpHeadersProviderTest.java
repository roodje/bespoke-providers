package com.yolt.providers.ing.common.pec;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.ing.common.IngSampleAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.pec.initiate.DefaultInitiatePaymentPreExecutionResult;
import com.yolt.providers.ing.common.pec.submit.DefaultSubmitPaymentPreExecutionResult;
import com.yolt.providers.ing.common.service.IngSigningUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultCommonHttpHeadersProviderTest {

    private DefaultCommonHttpHeadersProvider sut;

    @Mock
    private Signer signer;

    @Mock
    private IngSigningUtil ingSigningUtil;

    @Mock
    private Clock clock;

    @Mock
    private IngClientAccessMeans accessMeans;

    @Test
    void shouldProvideCommonHeadersForCorrectInputInInitPaymentFlow() throws IOException, URISyntaxException {
        // given
        var authMeansMap = (new IngSampleAuthenticationMeans()).getAuthenticationMeans();
        var authMeans = IngAuthenticationMeans.createIngAuthenticationMeans(authMeansMap, null);
        var preExecutionResult = createInitiatePreExecutionResult(authMeans);

        var expectedInstant = Clock.systemUTC().instant();
        var expectedStringInstant = DefaultCommonHttpHeadersProvider.ING_DATETIME_FORMATTER.format(expectedInstant);

        sut = new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, ExternalTracingUtil::createLastExternalTraceId);

        given(ingSigningUtil.getSignature(
                any(HttpMethod.class),
                anyString(),
                any(HttpHeaders.class),
                anyString(),
                eq(authMeans.getSigningKeyId()),
                eq(signer)
                )
        ).willReturn("testSignature");

        given(ingSigningUtil.getDigest(any(byte[].class)))
                .willReturn("testDigest");

        given(accessMeans.getAccessToken())
                .willReturn("testAccessToken");

        given(accessMeans.getClientId())
                .willReturn("testClientId");

        given(clock.instant())
                .willReturn(expectedInstant);

        // when
        var result = sut.provideHttpHeaders(preExecutionResult, new byte[0], HttpMethod.POST, "");

        // then
        assertThat(result.getFirst(DefaultCommonHttpHeadersProvider.X_REQUEST_ID_HEADER_NAME)).isNotNull();
        assertThat(result).extracting(
                HttpHeaders::getContentType,
                h -> h.getFirst(HttpHeaders.AUTHORIZATION),
                h -> h.getFirst(DefaultCommonHttpHeadersProvider.DATE_HEADER_NAME),
                h -> h.getFirst(DefaultCommonHttpHeadersProvider.SIGNATURE_HEADER_NAME),
                h -> h.getFirst(DefaultCommonHttpHeadersProvider.DIGEST_HEADER_NAME),
                h -> h.getFirst(DefaultCommonHttpHeadersProvider.PSU_IP_ADDRESS_HEADER_NAME)
        ).contains(
                MediaType.APPLICATION_JSON,
                "Bearer testAccessToken",
                expectedStringInstant,
                "testSignature",
                "testDigest",
                "fakePsuIpAddress"
        );
    }

    @Test
    void shouldProvideCommonHeadersForCorrectInputInSubmitPaymentFlow() throws IOException, URISyntaxException {
        // given
        var authMeansMap = (new IngSampleAuthenticationMeans()).getAuthenticationMeans();
        var authMeans = IngAuthenticationMeans.createIngAuthenticationMeans(authMeansMap, null);
        var preExecutionResult = createInitiatePreExecutionResult(authMeans);

        var expectedInstant = Clock.systemUTC().instant();
        var expectedStringInstant = DefaultCommonHttpHeadersProvider.ING_DATETIME_FORMATTER.format(expectedInstant);

        sut = new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, ExternalTracingUtil::createLastExternalTraceId);

        given(ingSigningUtil.getSignature(
                any(HttpMethod.class),
                anyString(),
                any(HttpHeaders.class),
                anyString(),
                eq(authMeans.getSigningKeyId()),
                eq(signer)
                )
        ).willReturn("testSignature");

        given(ingSigningUtil.getDigest(any(byte[].class)))
                .willReturn("testDigest");

        given(accessMeans.getAccessToken())
                .willReturn("testAccessToken");

        given(accessMeans.getClientId())
                .willReturn("testClientId");

        given(clock.instant())
                .willReturn(expectedInstant);

        // when
        var result = sut.provideHttpHeaders(preExecutionResult, new byte[0], HttpMethod.POST, "");

        // then
        assertThat(result).extracting(
                HttpHeaders::getContentType,
                h -> h.getFirst(HttpHeaders.AUTHORIZATION),
                h -> h.getFirst(DefaultCommonHttpHeadersProvider.DATE_HEADER_NAME),
                h -> h.getFirst(DefaultCommonHttpHeadersProvider.SIGNATURE_HEADER_NAME),
                h -> h.getFirst(DefaultCommonHttpHeadersProvider.DIGEST_HEADER_NAME),
                h -> h.getFirst(DefaultCommonHttpHeadersProvider.PSU_IP_ADDRESS_HEADER_NAME)
        ).contains(
                MediaType.APPLICATION_JSON,
                "Bearer testAccessToken",
                expectedStringInstant,
                "testSignature",
                "testDigest",
                "fakePsuIpAddress"
        );

    }

    private DefaultSubmitPaymentPreExecutionResult createSubmitPreExecutionResult(final IngAuthenticationMeans authMeans) {
        return new DefaultSubmitPaymentPreExecutionResult(
                "fakePaymentId",
                null,
                authMeans,
                accessMeans,
                signer,
                "fakePsuIpAddress",
                null
        );
    }

    private DefaultInitiatePaymentPreExecutionResult createInitiatePreExecutionResult(final IngAuthenticationMeans authMeans) {
        return new DefaultInitiatePaymentPreExecutionResult(
                SepaInitiatePaymentRequestDTO.builder().build(),
                null,
                authMeans,
                accessMeans,
                signer,
                "https://localhost.com",
                "fakeState",
                "fakePsuIpAddress"
        );
    }
}