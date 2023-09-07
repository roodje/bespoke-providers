package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import com.yolt.providers.openbanking.ais.rbsgroup.natwest.NatWestPropertiesV2;
import com.yolt.providers.openbanking.ais.rbsgroup.natwest.beanconfig.NatWestBeanConfigV2;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RbsGroupDataProviderAccessMeansTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final ObjectMapper OBJECT_MAPPER = new OpenbankingConfiguration().getObjectMapper();

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private String requestTraceId;
    private RbsGroupDataProviderV5 subject;

    @Mock
    private Signer signer;

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "12345";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis();

        subject = new NatWestBeanConfigV2().getNatWestDataProviderV11(new NatWestPropertiesV2(), null, Clock.systemUTC(), OBJECT_MAPPER);
    }

    @Test
    void shouldThrowExceptionWhenRefreshAccessMeansWithNullToken() throws Exception {
        // given
        AccessMeans oAuthToken = new AccessMeans(Instant.ofEpochMilli(0L),
                null,
                "test-accounts",
                null,
                new Date(),
                null,
                null);
        String serializedOAuthToken = OBJECT_MAPPER.writeValueAsString(oAuthToken);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> subject.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowTokenFailedExceptionWhenCreateAccessMeansWithInvalidGrantErrorInQueryParameters() {
        // given
        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545?error=invalid_grant";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setSigner(signer)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> subject.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    @Test
    void shouldThrowTokenFailedExceptionWhenCreateAccessMeansWithServerErrorInQueryParameters() {
        // given
        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545?error=server_error";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setSigner(signer)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> subject.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    @Test
    void shouldDeserializeTokenWithUnknownFields() throws IOException {
        // given
        String expectedAccessToken = "at12345";
        String expectedRefreshToken = "rt2345";
        String expectedExpireTime = "2018-01-11T12:13:14.123Z";
        String input = String.format(
                "{\"unknownField\": null, \"unknownField2\": null, \"accessToken\": \"%s\", \"refreshToken\": \"%s\", \"expireTime\": \"%s\"}",
                expectedAccessToken,
                expectedRefreshToken,
                expectedExpireTime);

        // when
        AccessMeans output = OBJECT_MAPPER.readValue(input, AccessMeans.class);

        // then
        assertThat(output.getAccessToken()).isEqualTo(expectedAccessToken);
        assertThat(output.getRefreshToken()).isEqualTo(expectedRefreshToken);
        assertThat(output.getExpireTime()).isEqualTo(Date.from(Instant.parse(expectedExpireTime)));
    }
}
