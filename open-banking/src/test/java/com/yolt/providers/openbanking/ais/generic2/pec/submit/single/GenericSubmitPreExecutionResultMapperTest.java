package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAccessTokenProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.GenericPaymentRequestInvocationException;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericSubmitPreExecutionResultMapperTest {

    @InjectMocks
    private GenericSubmitPreExecutionResultMapper subject;

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
    void shouldReturnGenericUkSubmitPreExecutionResultWhenCorrectDataAreProvided() throws TokenInvalidException, ConfirmationFailedException {
        // given
        Map<String, BasicAuthenticationMean> basicAuthenticationMeans = Collections.emptyMap();
        SubmitPaymentRequest submitPaymentRequest = prepareSubmitPaymentRequest(basicAuthenticationMeans);
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        AccessMeans accessMeans = prepareAccessMeans();
        UkProviderState providerState = new UkProviderState("", PaymentType.SINGLE, null);
        GenericSubmitPaymentPreExecutionResult expectedResult = GenericUkSubmitPaymentPreExecutionResultFixture
                .forUkDomesticSubmit(authMeans,
                        providerState,
                        "accessToken",
                        restTemplateManager,
                        signer);

        given(ukProviderStateDeserializer.deserialize(anyString()))
                .willReturn(providerState);
        given(getAuthenticationMeans.apply(anyMap()))
                .willReturn(authMeans);
        given(paymentAccessTokenProvider.provideUserAccessToken(any(RestTemplateManager.class), any(DefaultAuthMeans.class), anyString(), any(Signer.class), nullable(UUID.class)))
                .willReturn(accessMeans);

        // when
        GenericSubmitPaymentPreExecutionResult result = subject.map(submitPaymentRequest);

        // then
        then(ukProviderStateDeserializer)
                .should()
                .deserialize("providerState");
        then(getAuthenticationMeans)
                .should()
                .apply(basicAuthenticationMeans);
        then(paymentAccessTokenProvider)
                .should()
                .provideUserAccessToken(restTemplateManager, authMeans, "redirectUrlPostedBackFromSite", signer, null);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowGenericPaymentRequestInvocationExceptionWhenProvidingUserAccessTokenHasFailed() throws TokenInvalidException, ConfirmationFailedException {
        // given
        Map<String, BasicAuthenticationMean> basicAuthenticationMeans = Collections.emptyMap();
        SubmitPaymentRequest submitPaymentRequest = prepareSubmitPaymentRequest(basicAuthenticationMeans);
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        UkProviderState providerState = new UkProviderState("", PaymentType.SINGLE, null);

        given(ukProviderStateDeserializer.deserialize(anyString()))
                .willReturn(providerState);
        given(getAuthenticationMeans.apply(anyMap()))
                .willReturn(authMeans);
        given(paymentAccessTokenProvider.provideUserAccessToken(any(RestTemplateManager.class), any(DefaultAuthMeans.class), anyString(), any(Signer.class), nullable(UUID.class)))
                .willThrow(TokenInvalidException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.map(submitPaymentRequest);

        // then
        assertThatExceptionOfType(GenericPaymentRequestInvocationException.class)
                .isThrownBy(callable)
                .withCauseInstanceOf(TokenInvalidException.class);
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

    private SubmitPaymentRequest prepareSubmitPaymentRequest(Map<String, BasicAuthenticationMean> basicAuthenticationMeans) {
        return new SubmitPaymentRequest(
                "providerState",
                basicAuthenticationMeans,
                "redirectUrlPostedBackFromSite",
                signer,
                restTemplateManager,
                "10.0.0.1",
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())
        );
    }
}