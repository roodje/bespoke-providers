package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@Deprecated
public class StetTokenPaymentPreExecutionResultMapperTest {

    private static final String BASE_URL = "https://stetbank.com";
    private static final String TOKEN_URL = BASE_URL + "/token";

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Mock
    private DefaultProperties properties;

    private StetTokenPaymentPreExecutionResultMapper tokenPaymentPreExecutionResultMapper;

    @BeforeEach
    void initialize() {
        tokenPaymentPreExecutionResultMapper = new StetTokenPaymentPreExecutionResultMapper(properties);
    }

    @Test
    void shouldMapToTokenPaymentPreExecutionResultBasedOnInitiatePaymentRequest() {
        // given
        InitiatePaymentRequest request = new InitiatePaymentRequestBuilder()
                .setAuthenticationMeans(new HashMap<>())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        given(properties.getRegions())
                .willReturn(Collections.singletonList(createRegion()));

        // when
        StetTokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authenticationMeans);

        then(properties)
                .should()
                .getRegions();

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

        given(properties.getRegions())
                .willReturn(Collections.singletonList(createRegion()));

        // when
        StetTokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authenticationMeans);

        then(properties)
                .should()
                .getRegions();

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

        given(properties.getRegions())
                .willReturn(Collections.singletonList(createRegion()));

        // when
        StetTokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authenticationMeans);

        then(properties)
                .should()
                .getRegions();

        // then
        assertThat(preExecutionResult).satisfies(validateTokenPaymentPreExecutionResult());
    }

    private Consumer<StetTokenPaymentPreExecutionResult> validateTokenPaymentPreExecutionResult() {
        return preExecutionResult -> {
            assertThat(preExecutionResult.getRequestUrl()).isEqualTo(TOKEN_URL);
            assertThat(preExecutionResult.getBaseUrl()).isEqualTo(BASE_URL);
            assertThat(preExecutionResult.getRestTemplateManager()).isEqualTo(restTemplateManager);
            assertThat(preExecutionResult.getAuthMeans()).isEqualTo(authenticationMeans);
            assertThat(preExecutionResult.getSigner()).isEqualTo(signer);
        };
    }

    private Region createRegion() {
        Region region = new Region();
        region.setTokenUrl(TOKEN_URL);
        region.setBaseUrl(BASE_URL);
        return region;
    }
}
