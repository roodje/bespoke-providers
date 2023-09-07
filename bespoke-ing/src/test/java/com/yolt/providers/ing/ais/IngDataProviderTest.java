package com.yolt.providers.ing.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.ing.common.IngDataProviderV9;
import com.yolt.providers.ing.common.auth.IngAuthData;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.auth.IngUserAccessMeans;
import com.yolt.providers.ing.common.config.IngObjectMapper;
import com.yolt.providers.ing.common.dto.TestIngAuthData;
import com.yolt.providers.ing.common.dto.TestRedirectUrlResponse;
import com.yolt.providers.ing.common.service.IngAuthenticationServiceV3;
import com.yolt.providers.ing.common.service.IngFetchDataService;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.ing.common.auth.IngAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngDataProviderTest {

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final String EXAMPLE_TOKEN = "example-token";
    private static final String EXAMPLE_URL = "https://myaccount.ing.com/granting/";
    private static final String EXAMPLE_LOGIN_URL = "https://login.example.ing.com/";
    private static final UUID EXAMPLE_USER_ID = UUID.randomUUID();
    private static final String EXAMPLE_CLIENT_ID = "example_client_id";
    private Instant EXAMPLE_INSTANT = Clock.systemUTC().instant();

    private static final AuthenticationMeansReference AUTHENTICATION_MEANS_REFERENCE = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
    private static final String EXAMPLE_ACCESS_MEANS = "{\"accessToken\":\"example-token\",\"refreshToken\":null,\"tokenType\":\"Bearer\",\"expiryTimestamp\":1573027196000,\"refreshTokenExpiryTimestamp\":0,\"scope\":null,\"clientAccessMeans\":{\"accessToken\":\"example-token\",\"tokenType\":\"Bearer\",\"expiryTimestamp\":1573027196000,\"scope\":null,\"clientId\":\"example_client_id\",\"authenticationMeansReference\":{\"clientId\":\"" + CLIENT_ID_YOLT.toString() + "\",\"redirectUrlId\":\"" + CLIENT_REDIRECT_URL_ID_YOLT_APP + "\"}}}";

    @Mock
    private RestTemplateManager restTemplateManager;
    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private ObjectMapper objectMapper;

    @Mock
    private IngAuthenticationServiceV3 authenticationService;
    @Mock
    private IngFetchDataService dataService;

    @Mock
    private Clock clock;

    private IngDataProviderV9 subject;

    @BeforeEach
    public void beforeEach() throws IOException {
        objectMapper = IngObjectMapper.get();
        subject = new IngDataProviderV9(dataService, authenticationService, objectMapper, "IDENTIFIER", "IDENTIFIER_DISPLAYNAME", ProviderVersion.VERSION_1, clock);
        authenticationMeans = loadAuthenticationMeans();
    }

    @Test
    public void shouldReturnRedirectStepWithRedirectUrlForGetLoginInfoWithCorrectRequestData() throws TokenInvalidException {
        // given
        when(authenticationService.getIngRedirectUrl(any(IngClientAccessMeans.class), any(IngAuthenticationMeans.class), any(String.class), any(RestTemplateManager.class), any(Signer.class)))
                .thenReturn(new TestRedirectUrlResponse(EXAMPLE_URL));
        when(authenticationService.getLoginUrl(anyString(),
                anyString(), anyString(), anyString())).thenReturn(EXAMPLE_LOGIN_URL);
        when(authenticationService.getClientAccessMeans(any(IngAuthenticationMeans.class), any(AuthenticationMeansReference.class), any(RestTemplateManager.class), any(Signer.class))).thenReturn(prepareProperClientAccessMeans());
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeansReference(new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP))
                .setBaseClientRedirectUrl("http://yolt").setState("state")
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        RedirectStep loginInfo = subject.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(loginInfo.getRedirectUrl()).isEqualTo(EXAMPLE_LOGIN_URL);
    }

    @Test
    public void shouldReturnNewAccessMeansForCreateNewAccessMeansWithCorrectRequestData() throws JsonProcessingException, TokenInvalidException {
        // given
        when(authenticationService.getClientAccessMeans(any(IngAuthenticationMeans.class), any(AuthenticationMeansReference.class), any(RestTemplateManager.class), any(Signer.class))).thenReturn(prepareProperClientAccessMeans());
        when(authenticationService.getUserToken(any(IngClientAccessMeans.class), any(IngAuthenticationMeans.class), any(RestTemplateManager.class), any(Signer.class), anyString())).thenReturn(prepareProperUserAccessMeans());
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(EXAMPLE_USER_ID)
                .setRedirectUrlPostedBackFromSite("http://yolt?code=example-code")
                .setAuthenticationMeans(authenticationMeans)
                .setProviderState(objectMapper.writeValueAsString(prepareProperClientAccessMeans()))
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO accessMeans = subject.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        compareAccessMeansDTO(prepareProperAccessMeansDTO(), accessMeans.getAccessMeans());
    }

    @Test
    public void shouldReturnNewAccessMeansForRefreshAccessMeansWithCorrectRequestData() throws IOException, TokenInvalidException {
        // given
        IngClientAccessMeans clientAccessMeans = prepareProperClientAccessMeans();
        when(authenticationService.getClientAccessMeans(any(IngAuthenticationMeans.class), any(AuthenticationMeansReference.class), any(RestTemplateManager.class), any(Signer.class))).thenReturn(clientAccessMeans);
        when(authenticationService.refreshOAuthToken(eq(clientAccessMeans), any(IngUserAccessMeans.class), any(IngAuthenticationMeans.class), any(RestTemplateManager.class), any(Signer.class))).thenReturn(prepareProperUserAccessMeans());
        when(clock.instant()).thenReturn(Clock.systemUTC().instant());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(new AccessMeansDTO(EXAMPLE_USER_ID, EXAMPLE_ACCESS_MEANS, new Date(), new Date()))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        AccessMeansDTO accessMeans = subject.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        compareAccessMeansDTO(prepareProperAccessMeansDTO(), accessMeans);
    }

    @Test
    public void shouldThrowTokenInvalidExceptionForRefreshAccessMeansWhenInvalidToken() throws TokenInvalidException {
        // given
        IngClientAccessMeans clientAccessMeans = prepareProperClientAccessMeans();
        when(authenticationService.getClientAccessMeans(any(IngAuthenticationMeans.class), any(AuthenticationMeansReference.class), any(RestTemplateManager.class), any(Signer.class))).thenReturn(clientAccessMeans);
        when(authenticationService.refreshOAuthToken(any(IngClientAccessMeans.class), any(IngUserAccessMeans.class), any(IngAuthenticationMeans.class), any(RestTemplateManager.class), any(Signer.class)))
                .thenThrow(GetAccessTokenFailedException.class);
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(new AccessMeansDTO(EXAMPLE_USER_ID, EXAMPLE_ACCESS_MEANS, new Date(), new Date()))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> subject.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldReturnDataForFetchDataWithCorrectRequestData() throws ProviderFetchDataException, TokenInvalidException {
        // given
        DataProviderResponse mockResponse = mock(DataProviderResponse.class);
        when(dataService.fetchData(any(IngUserAccessMeans.class), any(IngAuthenticationMeans.class), any(RestTemplateManager.class), any(Signer.class), any(Instant.class), any(Clock.class))).thenReturn(mockResponse);

        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(new AccessMeansDTO(EXAMPLE_USER_ID, "{}", new Date(), new Date()))
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(EXAMPLE_INSTANT)
                .setSigner(signer)
                .build();
        // when
        DataProviderResponse response = subject.fetchData(urlFetchDataRequest);

        // then
        assertThat(response).isEqualTo(mockResponse);
    }

    private IngClientAccessMeans prepareProperClientAccessMeans() {
        return new IngClientAccessMeans(prepareProperTokenResponse(), AUTHENTICATION_MEANS_REFERENCE, Clock.systemUTC());
    }

    private IngUserAccessMeans prepareProperUserAccessMeans() {
        IngAuthData tokenResponse = prepareProperTokenResponse();
        return new IngUserAccessMeans(tokenResponse, new IngClientAccessMeans(tokenResponse, AUTHENTICATION_MEANS_REFERENCE, Clock.systemUTC()), Clock.systemUTC());
    }

    private IngAuthData prepareProperTokenResponse() {
        TestIngAuthData tokenResponse = new TestIngAuthData();
        tokenResponse.setAccessToken(EXAMPLE_TOKEN);
        tokenResponse.setClientId(EXAMPLE_CLIENT_ID);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(0L);
        return tokenResponse;
    }

    private AccessMeansDTO prepareProperAccessMeansDTO() {
        return new AccessMeansDTO(EXAMPLE_USER_ID, EXAMPLE_ACCESS_MEANS, new Date(), new Date());
    }

    private void compareAccessMeansDTO(final AccessMeansDTO expected, final AccessMeansDTO actual) throws JsonProcessingException {
        assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
        IngUserAccessMeans expectedAccessMeans = objectMapper.readValue(expected.getAccessMeans(), IngUserAccessMeans.class);
        IngUserAccessMeans actualAccessMeans = objectMapper.readValue(actual.getAccessMeans(), IngUserAccessMeans.class);
        assertThat(actualAccessMeans.getRefreshToken()).isEqualTo(expectedAccessMeans.getRefreshToken());
        assertThat(actualAccessMeans.getScope()).isEqualTo(expectedAccessMeans.getScope());
        assertThat(actualAccessMeans.getTokenType()).isEqualTo(expectedAccessMeans.getTokenType());
        IngClientAccessMeans expectedClientAccessMeans = expectedAccessMeans.getClientAccessMeans();
        IngClientAccessMeans actualClientAccessMeans = actualAccessMeans.getClientAccessMeans();
        assertThat(actualClientAccessMeans.getAccessToken()).isEqualTo(expectedClientAccessMeans.getAccessToken());
        assertThat(actualClientAccessMeans.getClientId()).isEqualTo(expectedClientAccessMeans.getClientId());
        assertThat(actualClientAccessMeans.getScope()).isEqualTo(expectedClientAccessMeans.getScope());
        assertThat(actualClientAccessMeans.getTokenType()).isEqualTo(expectedClientAccessMeans.getTokenType());
    }

    private static Map<String, BasicAuthenticationMean> loadAuthenticationMeans() throws IOException {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("example_client_signing.cer")));
        authenticationMeans.put(SIGNING_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "11111111-1111-1111-1111-111111111111"));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("example_client_tls.cer")));
        authenticationMeans.put(TRANSPORT_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "00000000-0000-0000-0000-000000000000"));
        return authenticationMeans;
    }

    private static String loadPemFile(final String fileName) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        URI uri = resourceLoader.getResource("classpath:certificates/" + fileName).getURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
