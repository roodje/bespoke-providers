package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAccessTokenProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled.GenericUkInitiatePaymentPreExecutionResultFixture;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericUkDomesticInitiatePaymentPreExecutionResultMapperTest {

    @InjectMocks
    private GenericUkDomesticInitiatePaymentPreExecutionResultMapper subject;

    @Mock
    private Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;

    @Mock
    private GenericPaymentAccessTokenProvider paymentAccessTokenProvider;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnGenericUkInitiatePaymentPreExecutionResultWhenCorrectDataAreProvided() {
        // given
        Map<String, BasicAuthenticationMean> basicAuthenticationMeans = Collections.emptyMap();
        AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());
        InitiateUkDomesticPaymentRequestDTO requestDTO = preparePaymentRequestDTO();
        InitiateUkDomesticPaymentRequest initiateUkDomesticPaymentRequest = prepareInitiateUkDomesticPaymentRequest(requestDTO, basicAuthenticationMeans, authenticationMeansReference);
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        AccessMeans accessMeans = prepareAccessMeans();
        GenericInitiatePaymentPreExecutionResult expectedResult = GenericUkInitiatePaymentPreExecutionResultFixture
                .forUkDomesticInitiation(authMeans,
                        "state",
                        "baseClientRedirectUrl",
                        requestDTO,
                        authenticationMeansReference,
                        "accessToken",
                        restTemplateManager,
                        signer);

        given(getAuthenticationMeans.apply(anyMap()))
                .willReturn(authMeans);
        given(paymentAccessTokenProvider.provideClientAccessToken(any(RestTemplateManager.class),
                any(DefaultAuthMeans.class),
                any(AuthenticationMeansReference.class),
                any(Signer.class)))
                .willReturn(accessMeans);

        // when
        GenericInitiatePaymentPreExecutionResult result = subject.map(initiateUkDomesticPaymentRequest);

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
    void shouldThrowCreationFailedExceptionWhenInitiateUkDomesticPaymentRequestIsNotValid() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.map(null);

        // then
        assertThatExceptionOfType(CreationFailedException.class)
                .isThrownBy(callable);
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

    private InitiateUkDomesticPaymentRequest prepareInitiateUkDomesticPaymentRequest(InitiateUkDomesticPaymentRequestDTO requestDTO,
                                                                                     Map<String, BasicAuthenticationMean> basicAuthenticationMeans,
                                                                                     AuthenticationMeansReference authenticationMeansReference) {
        return new InitiateUkDomesticPaymentRequest(
                requestDTO,
                "baseClientRedirectUrl",
                "state",
                basicAuthenticationMeans,
                signer,
                restTemplateManager,
                "10.0.0.1",
                authenticationMeansReference
        );
    }

    private InitiateUkDomesticPaymentRequestDTO preparePaymentRequestDTO() {
        return new InitiateUkDomesticPaymentRequestDTO("",
                "",
                new BigDecimal("100.00"),
                new UkAccountDTO("", AccountIdentifierScheme.IBAN, "Creditor", ""),
                null,
                "",
                null);
    }
}