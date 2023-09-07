package com.yolt.providers.openbanking.ais.tidegroup.ais;

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
import com.yolt.providers.openbanking.ais.tidegroup.TideGroupApp;
import com.yolt.providers.openbanking.ais.tidegroup.TideGroupSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.tidegroup.common.TideGroupDataProviderV2;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TideGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("tidegroup")
public class TideGroupDataProviderV3AccessMeansTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("TideDataProviderV3")
    private TideGroupDataProviderV2 tideDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<TideGroupDataProviderV2> getProviders() {
        return Stream.of(tideDataProvider);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = TideGroupSampleTypedAuthMeansV2.getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowExceptionWhenRefreshAccessMeansWithNullToken(TideGroupDataProviderV2 subject) throws Exception {
        // given
        AccessMeans oAuthToken = new AccessMeans(Instant.ofEpochMilli(0L),
                null,
                "test-accounts",
                null,
                new Date(),
                null,
                null);
        String serializedOAuthToken = objectMapper.writeValueAsString(oAuthToken);
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

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowTokenFailedExceptionWhenCreateAccessMeansWithInvalidGrantErrorInQueryParameters(TideGroupDataProviderV2 subject) {
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

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowTokenFailedExceptionWhenCreateAccessMeansWithServerErrorInQueryParameters(TideGroupDataProviderV2 subject) {
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
}
