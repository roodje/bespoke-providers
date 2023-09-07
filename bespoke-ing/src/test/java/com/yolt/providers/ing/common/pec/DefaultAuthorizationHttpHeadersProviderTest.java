package com.yolt.providers.ing.common.pec;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.ing.common.IngSampleAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.config.IngProperties;
import com.yolt.providers.ing.common.service.IngSigningUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultAuthorizationHttpHeadersProviderTest {

    @InjectMocks
    private DefaultAuthorizationHeadersProvider sut;

    @Mock
    private Signer signer;

    @Mock
    private IngSigningUtil ingSigningUtil;

    @Mock
    private IngProperties properties;

    @Mock
    private Clock clock;

    @Test
    void shouldProvideCommonHeadersForCorrectInputInInitPaymentFlow() throws IOException, URISyntaxException {
        // given
        var authMeansMap = (new IngSampleAuthenticationMeans()).getAuthenticationMeans();
        var authMeans = IngAuthenticationMeans.createIngAuthenticationMeans(authMeansMap, null);

        var expectedInstant = Clock.systemUTC().instant();
        var expectedStringInstant = DefaultAuthorizationHeadersProvider.ING_DATETIME_FORMATTER.format(expectedInstant);

        given(ingSigningUtil.getSignature(
                any(HttpMethod.class),
                anyString(),
                any(HttpHeaders.class),
                anyString(),
                eq(authMeans.getSigningKeyId()),
                eq(signer)
                )
        ).willReturn("testSignature");

        given(ingSigningUtil.getDigest(any(MultiValueMap.class)))
                .willReturn("testDigest");

        given(properties.getOAuthTokenEndpoint())
                .willReturn("/token");

        given(clock.instant())
                .willReturn(expectedInstant);


        // when
        var result = sut.provideHttpHeaders(new LinkedMultiValueMap<>(new HashMap<>()), authMeans, signer);

        // then
        assertThat(result).extracting(
                HttpHeaders::getContentType,
                h -> h.getFirst(HttpHeaders.AUTHORIZATION),
                h -> h.getFirst(HttpHeaders.DATE),
                h -> h.getFirst(DefaultAuthorizationHeadersProvider.DIGEST_HEADER_NAME),
                h -> h.getFirst(DefaultAuthorizationHeadersProvider.TPP_SIGNATURE_CERTIFICATE_HEADER_NAME)

        ).contains(
                MediaType.APPLICATION_FORM_URLENCODED,
                "Signature testSignature",
                expectedStringInstant,
                "testDigest",
                authMeans.getSigningCertificatePemFormat()
        );
    }
}