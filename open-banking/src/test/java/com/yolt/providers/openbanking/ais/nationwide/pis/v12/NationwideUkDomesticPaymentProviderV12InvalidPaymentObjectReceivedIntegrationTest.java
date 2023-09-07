package com.yolt.providers.openbanking.ais.nationwide.pis.v12;

import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.nationwide.NationwideApp;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test contains creation of uk payment when received invalid payment object in Nationwide.
 * <p>
 * Disclaimer: Nationwide is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - successful creation of payment
 * <p>
 */

@SpringBootTest(classes = {NationwideApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("nationwide")
class NationwideUkDomesticPaymentProviderV12InvalidPaymentObjectReceivedIntegrationTest {

    @Autowired
    @Qualifier("NationwidePaymentProviderV12")
    private GenericBasePaymentProviderV2 paymentProvider;

    @Test
    void shouldThrowPaymentExecutionTechnicalExceptionForCreatePaymentWhenInvalidPaymentObjectWasReceived() {
        // given-when
        ThrowableAssert.ThrowingCallable createPaymentCallable = () -> paymentProvider.initiateSinglePayment(null);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(createPaymentCallable)
                .withMessage("request_creation_error");
    }
}