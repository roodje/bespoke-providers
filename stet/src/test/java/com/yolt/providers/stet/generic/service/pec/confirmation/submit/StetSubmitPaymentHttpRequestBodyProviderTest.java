package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class StetSubmitPaymentHttpRequestBodyProviderTest {

    private static final String PSU_AUTHENTICATION_FACTOR = "as52n21h";
    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE = String.format("https://yolt.com/payment/?psuAuthenticationFactor=%s", PSU_AUTHENTICATION_FACTOR);

    @Mock
    private SepaSubmitPaymentAuthenticationFactorExtractor<StetConfirmationPreExecutionResult> authenticationFactorExtractor;

    private StetSubmitPaymentHttpRequestBodyProvider httpRequestBodyProvider;

    @BeforeEach
    void initialize() {
        httpRequestBodyProvider = new StetSubmitPaymentHttpRequestBodyProvider(authenticationFactorExtractor);
    }

    @Test
    void shouldProvideRequestBody() {
        // given
        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .redirectUrlPostedBackFromSite(REDIRECT_URL_POSTED_BACK_FROM_SITE)
                .build();

        given(authenticationFactorExtractor.extractAuthenticationFactor(any(StetConfirmationPreExecutionResult.class)))
                .willReturn(PSU_AUTHENTICATION_FACTOR);

        // when
        StetPaymentConfirmationRequestDTO requestDTO = httpRequestBodyProvider.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(requestDTO.getPsuAuthenticationFactor()).isEqualTo(PSU_AUTHENTICATION_FACTOR);

        then(authenticationFactorExtractor)
                .should()
                .extractAuthenticationFactor(preExecutionResult);
    }
}
