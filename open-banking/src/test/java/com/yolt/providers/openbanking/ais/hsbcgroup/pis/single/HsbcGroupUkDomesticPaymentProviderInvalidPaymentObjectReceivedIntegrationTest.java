package com.yolt.providers.openbanking.ais.hsbcgroup.pis.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
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
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = "classpath:/stubs/hsbcgroup/pis-3.1.6/single", httpsPort = 0, port = 0)
class HsbcGroupUkDomesticPaymentProviderInvalidPaymentObjectReceivedIntegrationTest {

    @Autowired
    @Qualifier("HsbcPaymentProviderV13")
    private GenericBasePaymentProviderV2 hsbcPaymentProviderV13;

    @Autowired
    @Qualifier("HsbcPaymentProviderV14")
    private GenericBasePaymentProviderV3 hsbcPaymentProviderV14;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV13")
    private GenericBasePaymentProviderV2 firstDirectPaymentProviderV13;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV14")
    private GenericBasePaymentProviderV3 firstDirectPaymentProviderV14;

    private Stream<UkDomesticPaymentProvider> getProviders() {
        return Stream.of(hsbcPaymentProviderV13, firstDirectPaymentProviderV13,
                hsbcPaymentProviderV14, firstDirectPaymentProviderV14);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionForCreatePaymentWhenInvalidPaymentObjectWasReceived(UkDomesticPaymentProvider paymentProvider) {
        // given
        // when
        ThrowableAssert.ThrowingCallable createPaymentCallable = () -> paymentProvider.initiateSinglePayment(null);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(createPaymentCallable)
                .withMessage("request_creation_error");
    }
}