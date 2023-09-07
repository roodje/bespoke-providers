package com.yolt.providers.openbanking.ais.bankofirelandgroup.pis;

import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandGroupApp;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test contains creation of uk payment when received invalid payment object in HSBC.
 * <p>
 * Covered flows:
 * - successful creation of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BankOfIrelandGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bankofireland")
@AutoConfigureWireMock(stubs = "classpath:/stubs/bankofireland/pis-3.0.0/single", httpsPort = 0, port = 0)
class BankOfIrelandUkDomesticPaymentProviderInvalidPaymentObjectReceivedIntegrationTest {

    @Autowired
    @Qualifier("BankOfIrelandPaymentProviderV1")
    private GenericBasePaymentProviderV3 bankOfIrelandPaymentProviderV1;

    private Stream<UkDomesticPaymentProvider> getProviders() {
        return Stream.of(bankOfIrelandPaymentProviderV1);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionForInitiateSinglePaymentWhenInvalidPaymentObjectWasReceived(UkDomesticPaymentProvider paymentProvider) {
        // given
        // when
        ThrowableAssert.ThrowingCallable createPaymentCallable = () -> paymentProvider.initiateSinglePayment(null);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(createPaymentCallable)
                .withMessage("request_creation_error");
    }
}