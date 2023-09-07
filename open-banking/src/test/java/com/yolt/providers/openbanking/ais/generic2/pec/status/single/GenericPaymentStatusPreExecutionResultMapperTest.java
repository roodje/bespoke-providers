package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAccessTokenProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericPaymentStatusPreExecutionResultMapperTest {

    @InjectMocks
    private GenericPaymentStatusPreExecutionResultMapper subject;

    @Mock
    private Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;

    @Mock
    private GenericPaymentAccessTokenProvider paymentAccessTokenProvider;

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private UkProviderStateDeserializer ukProviderStateDeserializer;

    @Test
    void shouldReturnGenericStatusPaymentPreExecutionResultWithPaymentIdOnlyWhenCorrectDataAreProvided() {
        // given
        Map<String, BasicAuthenticationMean> basicAuthenticationMeans = Collections.emptyMap();
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());
        GetStatusRequest getStatusRequest = prepareGetStatusRequest(basicAuthenticationMeans, authenticationMeansReference, null, "paymentId");
        AccessMeans accessMeans = prepareAccessMeans();
        GenericPaymentStatusPreExecutionResult expectedResult = GenericStatusPaymentPreExecutionResultFixture
                .forUkDomesticStatus(authMeans,
                        "accessToken",
                        "paymentId",
                        "",
                        restTemplateManager);

        given(getAuthenticationMeans.apply(anyMap()))
                .willReturn(authMeans);
        given(paymentAccessTokenProvider.provideClientAccessToken(any(RestTemplateManager.class), any(DefaultAuthMeans.class), any(AuthenticationMeansReference.class), any(Signer.class)))
                .willReturn(accessMeans);

        // when
        GenericPaymentStatusPreExecutionResult result = subject.map(getStatusRequest);

        // then
        then(getAuthenticationMeans)
                .should()
                .apply(basicAuthenticationMeans);
        then(paymentAccessTokenProvider)
                .should()
                .provideClientAccessToken(restTemplateManager, authMeans, authenticationMeansReference, signer);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldReturnGenericStatusPaymentPreExecutionResultWithConsentIdOnlyWhenCorrectDataAreProvided() {
        // given
        Map<String, BasicAuthenticationMean> basicAuthenticationMeans = Collections.emptyMap();
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());
        GetStatusRequest getStatusRequest = prepareGetStatusRequest(basicAuthenticationMeans, authenticationMeansReference, "consentId", null);
        AccessMeans accessMeans = prepareAccessMeans();
        UkProviderState ukProviderState = new UkProviderState("consentId", PaymentType.SINGLE, null);
        GenericPaymentStatusPreExecutionResult expectedResult = GenericStatusPaymentPreExecutionResultFixture
                .forUkDomesticStatus(authMeans,
                        "accessToken",
                        null,
                        "consentId",
                        restTemplateManager);

        given(ukProviderStateDeserializer.deserialize(anyString()))
                .willReturn(ukProviderState);
        given(getAuthenticationMeans.apply(anyMap()))
                .willReturn(authMeans);
        given(paymentAccessTokenProvider.provideClientAccessToken(any(RestTemplateManager.class), any(DefaultAuthMeans.class), any(AuthenticationMeansReference.class), any(Signer.class)))
                .willReturn(accessMeans);

        // when
        GenericPaymentStatusPreExecutionResult result = subject.map(getStatusRequest);

        // then
        then(ukProviderStateDeserializer)
                .should()
                .deserialize(prepareUkProviderState(ukProviderState));
        then(getAuthenticationMeans)
                .should()
                .apply(basicAuthenticationMeans);
        then(paymentAccessTokenProvider)
                .should()
                .provideClientAccessToken(restTemplateManager, authMeans, authenticationMeansReference, signer);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    private AccessMeans prepareAccessMeans() {
        return new AccessMeans(null,
                null,
                "accessToken",
                null,
                null,
                null,
                null);
    }

    private GetStatusRequest prepareGetStatusRequest(Map<String, BasicAuthenticationMean> basicAuthenticationMeans,
                                                     AuthenticationMeansReference authenticationMeansReference,
                                                     String consentId,
                                                     String paymentId) {
        return new GetStatusRequest(consentId != null && !consentId.isEmpty() ? prepareUkProviderState(new UkProviderState(consentId, PaymentType.SINGLE, null)) : null,
                paymentId,
                basicAuthenticationMeans,
                signer,
                restTemplateManager,
                null,
                authenticationMeansReference);
    }

    private String prepareUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}