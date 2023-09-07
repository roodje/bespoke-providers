package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;

import java.util.Collections;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.SUBMIT_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@Deprecated
public class StetSubmitPaymentHttpRequestInvokerTest {

    private static final String PROVIDER_IDENTIFIER = "STET_PROVIDER";
    private static final String PROVIDER_DISPLAY_NAME = "Stet Provider";
    private static final String BASE_URL = "https://stetbank.com";
    private static final String REQUEST_URL = BASE_URL + "/initiate-payment";

    @Mock
    private DefaultProperties properties;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private HttpErrorHandler httpErrorHandler;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Mock
    private DefaultHttpClient httpClient;

    private StetSubmitPaymentHttpRequestInvoker stetStatusPaymentHttpRequestInvoker;

    @BeforeEach
    void initialize() {
        ProviderIdentification providerIdentification = new ProviderIdentification(
                PROVIDER_IDENTIFIER,
                PROVIDER_DISPLAY_NAME,
                ProviderVersion.VERSION_1);

        stetStatusPaymentHttpRequestInvoker = new StetSubmitPaymentHttpRequestInvoker(
                httpClientFactory,
                providerIdentification,
                httpErrorHandler,
                properties);
    }

    @Test
    void shouldInvokeRequestAndReturnPaymentStatusResponseAsJsonNode() throws TokenInvalidException {
        // given
        HttpEntity<StetPaymentConfirmationRequestDTO> httpEntity = createHttpEntity();

        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .httpMethod(HttpMethod.POST)
                .requestPath(REQUEST_URL)
                .authMeans(authenticationMeans)
                .restTemplateManager(restTemplateManager)
                .build();

        JsonNode expectedJsonNode = JsonNodeFactory.instance.textNode("Payment Initiation");

        given(properties.getRegions())
                .willReturn(Collections.singletonList(createRegion()));
        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthenticationMeans.class), anyString(), anyString()))
                .willReturn(httpClient);
        given(httpClient.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), anyString(), eq(JsonNode.class), any(HttpErrorHandler.class)))
                .willReturn(ResponseEntity.ok(expectedJsonNode));

        // when
        ResponseEntity<JsonNode> responseEntity = stetStatusPaymentHttpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expectedJsonNode);

        then(properties)
                .should()
                .getRegions();
        then(httpClientFactory)
                .should()
                .createHttpClient(restTemplateManager, authenticationMeans, BASE_URL, PROVIDER_DISPLAY_NAME);
        then(httpClient)
                .should()
                .exchange(REQUEST_URL, HttpMethod.POST, httpEntity, SUBMIT_PAYMENT, JsonNode.class, httpErrorHandler);
    }

    private HttpEntity<StetPaymentConfirmationRequestDTO> createHttpEntity() {
        StetPaymentConfirmationRequestDTO requestDTO = StetPaymentConfirmationRequestDTO.builder()
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(requestDTO, httpHeaders);
    }

    private Region createRegion() {
        Region region = new Region();
        region.setBaseUrl(BASE_URL);
        return region;
    }
}
