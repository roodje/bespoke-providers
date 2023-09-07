package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StetSubmitPaymentAuthenticationFactorExtractorTest {

    private static final String PSU_AUTHENTICATION_FACTOR = "as52n21h";
    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE = String.format("https://yolt.com/payment/?psuAuthenticationFactor=%s", PSU_AUTHENTICATION_FACTOR);

    private StetSubmitPaymentAuthenticationFactorExtractor authenticationFactorExtractor;

    @BeforeEach
    void initialize() {
        authenticationFactorExtractor = new StetSubmitPaymentAuthenticationFactorExtractor();
    }

    @Test
    void asd() {
        // given
        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .redirectUrlPostedBackFromSite(REDIRECT_URL_POSTED_BACK_FROM_SITE)
                .build();

        // when
        String authenticationFactor = authenticationFactorExtractor.extractAuthenticationFactor(preExecutionResult);

        // then
        assertThat(authenticationFactor).isEqualTo(PSU_AUTHENTICATION_FACTOR);
    }
}
