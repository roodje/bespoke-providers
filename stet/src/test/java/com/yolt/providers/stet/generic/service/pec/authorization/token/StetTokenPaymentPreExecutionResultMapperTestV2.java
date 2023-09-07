package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StetTokenPaymentPreExecutionResultMapperTestV2 {

    private static final String BASE_URL = "https://stetbank.com";
    private static final String TOKEN_URL = BASE_URL + "/token";

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private HttpClient httpClient;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    private Region region;

    private StetTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper;

    @BeforeEach
    void initialize() {
        region = new Region();
        region.setTokenUrl(TOKEN_URL);
        tokenPaymentPreExecutionResultMapper = new StetTokenPaymentPreExecutionResultMapperV2();
    }

    @Test
    void shouldMapToTokenPaymentPreExecutionResultBasedOnInitiatePaymentRequest() {
        // given
        InitiatePaymentRequest request = new InitiatePaymentRequestBuilder()
                .setAuthenticationMeans(new HashMap<>())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        StetTokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authenticationMeans, httpClient, region);


        // then
        assertThat(preExecutionResult).satisfies(validateTokenPaymentPreExecutionResult());
    }

    @Test
    void shouldMapToTokenPaymentPreExecutionResultBasedOnGetStatusRequest() {
        // given
        GetStatusRequest request = new GetStatusRequestBuilder()
                .setAuthenticationMeans(new HashMap<>())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        StetTokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authenticationMeans, httpClient, region);

        // then
        assertThat(preExecutionResult).satisfies(validateTokenPaymentPreExecutionResult());
    }

    @Test
    void shouldMapToTokenPaymentPreExecutionResultBasedOnSubmitPaymentRequest() {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(new HashMap<>())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();


        // when
        StetTokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authenticationMeans, httpClient, region);


        // then
        assertThat(preExecutionResult).satisfies(validateTokenPaymentPreExecutionResult());
    }

    private Consumer<StetTokenPaymentPreExecutionResult> validateTokenPaymentPreExecutionResult() {
        return preExecutionResult -> {
            assertThat(preExecutionResult.getRequestUrl()).isEqualTo(TOKEN_URL);
            assertThat(preExecutionResult.getHttpClient()).isEqualTo(httpClient);
            assertThat(preExecutionResult.getAuthMeans()).isEqualTo(authenticationMeans);
            assertThat(preExecutionResult.getSigner()).isEqualTo(signer);
        };
    }

}
