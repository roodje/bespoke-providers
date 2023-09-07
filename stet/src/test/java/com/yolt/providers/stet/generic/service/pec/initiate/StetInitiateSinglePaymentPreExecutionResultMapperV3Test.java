package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResultMapperV2;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class StetInitiateSinglePaymentPreExecutionResultMapperV3Test {

    private static final String PROVIDER_IDENTIFIER = "STET_PROVIDER";
    private static final String PROVIDER_DISPLAY_NAME = "Stet Provider";
    private static final String BASE_CLIENT_REDIRECT_URL = "https://stetbank.com";
    private static final String PAYMENT_INITIATION_PATH = "/payment-requests";
    private static final String BANK_BASE_URL = "https://baseUrl.com";
    private static final String STATE = "bc05af42-3358-452b-bcdb-32c6234161c7";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCESS_TOKEN = "cdbcd575-046a-4035-916a-85efc92d63ef";

    @Mock
    private AuthenticationMeansSupplier authenticationMeansSupplier;

    @Mock
    private StetTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper;

    @Mock
    private SepaTokenPaymentHttpRequestInvoker<StetTokenPaymentPreExecutionResult> tokenHttpRequestInvoker;

    @Mock
    private Signer signer;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private HttpClient httpClient;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Mock
    private TokenResponseDTO tokenResponseDTO;

    @Mock
    private StetTokenPaymentPreExecutionResult tokenPaymentPreExecutionResult;

    private Region region;

    private StetInitiateSinglePaymentPreExecutionResultMapperV3 initiatePaymentPreExecutionResultMapper;

    @BeforeEach
    void initialize() {
        ProviderIdentification providerIdentification = new ProviderIdentification(
                PROVIDER_IDENTIFIER,
                PROVIDER_DISPLAY_NAME,
                ProviderVersion.VERSION_1);

        region = new Region();
        region.setBaseUrl(BANK_BASE_URL);
        DefaultProperties properties = new DefaultProperties();
        properties.setRegions(List.of(region));

        Function<Region, String> regionStringFunction = reg -> reg.getBaseUrl();

        initiatePaymentPreExecutionResultMapper = new StetInitiateSinglePaymentPreExecutionResultMapperV3(
                authenticationMeansSupplier,
                providerIdentification,
                tokenPaymentPreExecutionResultMapper,
                tokenHttpRequestInvoker,
                httpClientFactory,
                regionStringFunction,
                properties);
    }

    @Test
    void shouldMapToInitiatePreExecutionResult() {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>();
        InitiatePaymentRequest request = createInitiatePaymentRequest();

        given(authenticationMeansSupplier.getAuthMeans(any(), anyString()))
                .willReturn(authenticationMeans);
        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthenticationMeans.class), any(String.class), any(String.class)))
                .willReturn(httpClient);
        given(tokenPaymentPreExecutionResultMapper.map(any(InitiatePaymentRequest.class), any(DefaultAuthenticationMeans.class), any(HttpClient.class), any(Region.class)))
                .willReturn(tokenPaymentPreExecutionResult);
        given(tokenHttpRequestInvoker.invokeRequest(any(StetTokenPaymentPreExecutionResult.class)))
                .willReturn(tokenResponseDTO);
        given(tokenResponseDTO.getAccessToken())
                .willReturn(ACCESS_TOKEN);

        // when
        StetInitiatePreExecutionResult preExecutionResult = initiatePaymentPreExecutionResultMapper.map(request);

        // then
        assertThat(preExecutionResult.getAuthMeans()).isEqualTo(authenticationMeans);
        assertThat(preExecutionResult.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(preExecutionResult.getPsuIpAddress()).isEqualTo(PSU_IP_ADDRESS);
        assertThat(preExecutionResult.getRequestPath()).isEqualTo(PAYMENT_INITIATION_PATH);
        assertThat(preExecutionResult.getHttpMethod()).isEqualTo(HttpMethod.POST);
        assertThat(preExecutionResult.getSepaRequestDTO()).isEqualTo(request.getRequestDTO());
        assertThat(preExecutionResult.getHttpClient()).isEqualTo(httpClient);
        assertThat(preExecutionResult.getState()).isEqualTo(STATE);
        assertThat(preExecutionResult.getSigner()).isEqualTo(signer);
        assertThat(preExecutionResult.getBaseClientRedirectUrl()).isEqualTo(BASE_CLIENT_REDIRECT_URL);

        then(authenticationMeansSupplier)
                .should()
                .getAuthMeans(authMeans, PROVIDER_IDENTIFIER);
        then(httpClientFactory)
                .should()
                .createHttpClient(restTemplateManager, authenticationMeans, BANK_BASE_URL, PROVIDER_DISPLAY_NAME);
        then(tokenPaymentPreExecutionResultMapper)
                .should()
                .map(request, authenticationMeans, httpClient, region);
        then(tokenHttpRequestInvoker)
                .should()
                .invokeRequest(tokenPaymentPreExecutionResult);
        then(tokenResponseDTO)
                .should()
                .getAccessToken();
    }

    private InitiatePaymentRequest createInitiatePaymentRequest() {
        return new InitiatePaymentRequest(
                SepaInitiatePaymentRequestDTO.builder().build(),
                BASE_CLIENT_REDIRECT_URL,
                STATE,
                new HashMap<>(),
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()));
    }
}
