package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentHttpRequestInvokerV2;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResultMapperV2;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
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
public class StetStatusPaymentPreExecutionResultMapperV2Test {

    private static final String PROVIDER_IDENTIFIER = "STET_PROVIDER";
    private static final String PROVIDER_DISPLAY_NAME = "Stet Provider";
    private static final String PAYMENT_ID = "4523782332";
    private static final String PROVIDER_STATE = String.format("{\"paymentId\":\"%s\"}", PAYMENT_ID);
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCESS_TOKEN = "cdbcd575-046a-4035-916a-85efc92d63ef";
    private static final String PAYMENT_STATUS_ENDPOINT = String.format("/payment-requests/%s", PAYMENT_ID);
    private static final String BANK_BASE_URL = "https://baseUrl.com";

    @Mock
    private StetTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper;

    @Mock
    private StetTokenPaymentHttpRequestInvokerV2 tokenHttpRequestInvoker;

    @Mock
    private AuthenticationMeansSupplier authenticationMeansSupplier;

    @Mock
    private ProviderStateMapper providerStateMapper;

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private HttpClient httpClient;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Mock
    private TokenResponseDTO tokenResponseDTO;

    @Mock
    private StetTokenPaymentPreExecutionResult tokenPaymentPreExecutionResult;

    Region region;

    private StetStatusPaymentPreExecutionResultMapperV2 statusPaymentPreExecutionResultMapper;

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

        Function<Region, String> regionStringFunction = Region::getBaseUrl;

        statusPaymentPreExecutionResultMapper = new StetStatusPaymentPreExecutionResultMapperV2(
                authenticationMeansSupplier,
                providerIdentification,
                tokenPaymentPreExecutionResultMapper,
                tokenHttpRequestInvoker,
                providerStateMapper,
                httpClientFactory,
                regionStringFunction,
                properties);
    }

    @Test
    void shouldMapToStetConfirmationPreExecutionResult() {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>();
        GetStatusRequest request = createGetStatusRequest(authMeans);

        given(authenticationMeansSupplier.getAuthMeans(any(), anyString()))
                .willReturn(authenticationMeans);
        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthenticationMeans.class), anyString(), anyString()))
                .willReturn(httpClient);
        given(tokenPaymentPreExecutionResultMapper.map(any(GetStatusRequest.class), any(DefaultAuthenticationMeans.class), any(HttpClient.class), any(Region.class)))
                .willReturn(tokenPaymentPreExecutionResult);
        given(tokenHttpRequestInvoker.invokeRequest(any(StetTokenPaymentPreExecutionResult.class)))
                .willReturn(tokenResponseDTO);
        given(tokenResponseDTO.getAccessToken())
                .willReturn(ACCESS_TOKEN);

        // when
        StetConfirmationPreExecutionResult preExecutionResult = statusPaymentPreExecutionResultMapper.map(request);

        // then
        assertThat(preExecutionResult.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(preExecutionResult.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(preExecutionResult.getRequestPath()).isEqualTo(PAYMENT_STATUS_ENDPOINT);
        assertThat(preExecutionResult.getSigner()).isEqualTo(signer);
        assertThat(preExecutionResult.getPsuIpAddress()).isEqualTo(PSU_IP_ADDRESS);
        assertThat(preExecutionResult.getAuthMeans()).isEqualTo(authenticationMeans);
        assertThat(preExecutionResult.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(preExecutionResult.getHttpClient()).isEqualTo(httpClient);

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

    private GetStatusRequest createGetStatusRequest(Map<String, BasicAuthenticationMean> authMeans) {
        return new GetStatusRequest(
                PROVIDER_STATE,
                PAYMENT_ID,
                authMeans,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()));
    }
}
