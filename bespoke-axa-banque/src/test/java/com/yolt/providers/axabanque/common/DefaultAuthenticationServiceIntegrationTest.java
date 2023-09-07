package com.yolt.providers.axabanque.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.axabanque.common.fixtures.AuthMeansFixture;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.axabanque.common.fixtures.ProviderStateFixture.createProviderState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/v1/authorization/", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultAuthenticationServiceIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CLIENT_ID = "PSDFI-FINFSA-29884997";
    private static final String ACCESS_TOKEN = "THE-ACCESS-TOKEN";
    private static final UUID CODE = UUID.fromString("11111aa1-0000-1111-2222-3a333aa3aa3a");
    private static final String TRANSPORT_KEY_ID_VALUE = UUID.randomUUID().toString();
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = AuthMeansFixture.getAuthMeansMap(TRANSPORT_KEY_ID_VALUE);

    @Autowired
    @Qualifier("AxaGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Value("${wiremock.server.port}")
    private int port;

    @Autowired
    @Qualifier("ComdirectDataProviderV1")
    private UrlDataProvider comdirectDataProviderV1;

    @Autowired
    @Qualifier("AxaBeDataProviderV1")
    private UrlDataProvider axaBeDataProviderV1;

    @Autowired
    @Qualifier("BpostDataProviderV1")
    private UrlDataProvider bpostDataProviderV1;

    @Autowired
    @Qualifier("CrelanDataProviderV1")
    private UrlDataProvider crelanDataProviderV1;

    @Autowired
    @Qualifier("KeytradeDataProviderV1")
    private UrlDataProvider keytradeDataProviderV1;

    private Stream<UrlDataProvider> getAllAxaDataProviders() {
        return Stream.of(comdirectDataProviderV1, axaBeDataProviderV1, bpostDataProviderV1, crelanDataProviderV1, keytradeDataProviderV1);
    }

    @Autowired
    private RestTemplateManager restTemplateManager;

    @ParameterizedTest
    @MethodSource("getAllAxaDataProviders")
    void shouldReturnCorrectLoginUrl(UrlDataProvider dataProvider) {
        // given
        String state = "11a1aaa1-aa1a-11a1-a111-a1a11a11aa11";
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL + "?state=" + state)
                .setState(state)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep step = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        assertThat(step.getExternalConsentId()).isEqualTo("1111111111-aa11111");
        assertThat(step.getProviderState()).isNotEmpty();
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(step.getRedirectUrl()).build();
        assertThat(uriComponents.getPath()).isEqualTo("/authorize/aaaaaaa1-1111-1111-aaaa-111111111111");
        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        assertThat(queryParams.getFirst("client_id")).isEqualTo(CLIENT_ID);
        assertThat(queryParams.getFirst("response_type")).isEqualTo("code");
        assertThat(queryParams.getFirst("scope")).isEqualTo("AIS:1111111111-aa11111");
        assertThat(queryParams.getFirst("redirect_uri")).isEqualTo(REDIRECT_URL + "?state=" + state);
        assertThat(queryParams.getFirst("state")).isEqualTo(state);
    }

    @ParameterizedTest
    @MethodSource("getAllAxaDataProviders")
    void shouldCreateNewAccessMeansSuccessfully(UrlDataProvider dataProvider) throws IOException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setBaseClientRedirectUrl("https:baseUri.com")
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + CODE)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(objectMapper.writeValueAsString(
                        createProviderState("codeVerifier", "code", "consentId", "traceId", 1l)))
                .build();
        // when
        AccessMeansOrStepDTO accessMeans = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        GroupAccessMeans axaAccessMeans = objectMapper.readValue(accessMeans.getAccessMeans().getAccessMeans(), GroupAccessMeans.class);
        assertThat(axaAccessMeans.getAccessToken().getToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(axaAccessMeans.getBaseRedirectUri()).isEqualTo("https:baseUri.com");
        assertThat(axaAccessMeans.getProviderState())
                .isEqualToIgnoringGivenFields(
                        createProviderState("codeVerifier", "code", "consentId", "traceId", 11),
                        ", traceId", "consentGeneratedAt");
    }

    @ParameterizedTest
    @MethodSource("getAllAxaDataProviders")
    void shouldRefreshAccessMeansSuccessfully(UrlDataProvider dataProvider) throws IOException, TokenInvalidException {
        // given
        String expectedAccessToken = "THE-SUPER-BRAND-NEW-SHINY-ACCESS-TOKEN";
        String accessMeans = "{\"baseRedirectUri\":\"https:baseUri.com\",\"providerState\":{\"codeVerifier\":\"codeVerifier\",\"code\":\"code\",\"consentId\":\"consentId\",\"traceId\":\"traceId\",\"consentGeneratedAt\":1},\"accessToken\":{\"expiresIn\":900,\"refreshToken\":\"THE-REFRESH-TOKEN\",\"scope\":\"token scope\",\"tokenType\":\"Bearer\",\"token\":\"THE-ACCESS-TOKEN\"}}";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO obtained = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(obtained.getUserId()).isEqualTo(USER_ID);
        GroupAccessMeans accessTokenResponseDTO = objectMapper.readValue(obtained.getAccessMeans(), GroupAccessMeans.class);
        assertThat(accessTokenResponseDTO.getAccessToken().getToken()).isEqualTo(expectedAccessToken);
        assertThat(accessTokenResponseDTO.getAccessToken().getTokenType()).isEqualTo("Bearer");
        assertThat(accessTokenResponseDTO.getAccessToken().getRefreshToken()).isEqualTo("THE-REFRESH-TOKEN");
    }

    @ParameterizedTest
    @MethodSource("getAllAxaDataProviders")
    void shouldDeleteConsentSuccessfully(UrlDataProvider provider) {
        //given
        String accessMeans = "{\"baseRedirectUri\":\"https:baseUri.com\",\"providerState\":{\"codeVerifier\":\"codeVerifier\",\"code\":\"code\",\"consentId\":\"consentId\",\"traceId\":\"traceId\",\"consentGeneratedAt\":1},\"accessToken\":{\"expiresIn\":900,\"refreshToken\":\"THE-REFRESH-TOKEN\",\"scope\":\"token scope\",\"tokenType\":\"Bearer\",\"token\":\"THE-ACCESS-TOKEN\"}}";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());
        UrlOnUserSiteDeleteRequest request = new UrlOnUserSiteDeleteRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        ThrowableAssert.ThrowingCallable throwable = () -> provider.onUserSiteDelete(request);

        //then
        assertThatCode(throwable).doesNotThrowAnyException();

    }
}
