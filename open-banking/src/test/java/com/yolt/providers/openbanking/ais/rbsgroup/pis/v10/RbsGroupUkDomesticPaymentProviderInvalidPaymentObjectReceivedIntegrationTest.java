package com.yolt.providers.openbanking.ais.rbsgroup.pis.v10;

import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test contains creation of uk payment when received invalid payment object in RBS.
 * <p>
 * Covered flows:
 * - successful creation of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {RbsApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/pis", httpsPort = 0, port = 0)
class RbsGroupUkDomesticPaymentProviderInvalidPaymentObjectReceivedIntegrationTest {

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    @Autowired
    @Qualifier("NatWestPaymentProviderV11")
    private GenericBasePaymentProviderV2 natWestPaymentProviderV11;

    @Autowired
    @Qualifier("RoyalBankOfScotlandPaymentProviderV11")
    private GenericBasePaymentProviderV2 royalBankOfScotlandPaymentProviderV11;

    @Autowired
    @Qualifier("UlsterBankPaymentProviderV10")
    private GenericBasePaymentProviderV2 ulsterBankPaymentProviderV10;

    private Stream<UkDomesticPaymentProvider> getPecAwareProviders() {
        return Stream.of(natWestPaymentProviderV11,
                royalBankOfScotlandPaymentProviderV11,
                ulsterBankPaymentProviderV10);
    }

    @ParameterizedTest
    @MethodSource("getPecAwareProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionForInitiateSinglePaymentWhenInvalidPaymentObjectWasReceived(UkDomesticPaymentProvider paymentProvider) {
        // given
        // when
        ThrowableAssert.ThrowingCallable createPaymentCallable = () -> paymentProvider.initiateSinglePayment(null);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(createPaymentCallable)
                .withMessage("request_creation_error");
    }

    @ParameterizedTest
    @MethodSource("getPecAwareProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionWithIllegalArgumentExceptionAsCauseForInitiateSinglePaymentWhenEndToEndIdentificationIsTooLong(UkDomesticPaymentProvider paymentProvider) throws IOException, URISyntaxException {
        // given
        InitiateUkDomesticPaymentRequest request = createInitiateUkDomesticPaymentRequest(createInitiateUkDomesticPaymentRequestDTOWithTooLongEndToEndIdentification());

        // when
        ThrowableAssert.ThrowingCallable createPaymentCallable = () -> paymentProvider.initiateSinglePayment(request);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(createPaymentCallable)
                .withMessage("request_creation_error")
                .withCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("EndToEndIdentification in payment is too long (39), maximum allowed for RBS is 30 characters"));
    }

    @ParameterizedTest
    @MethodSource("getPecAwareProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionWithIllegalArgumentExceptionAsCauseForInitiateSinglePaymentWhenRemittanceInformationReferenceDoesntMatchPattern(UkDomesticPaymentProvider paymentProvider) throws IOException, URISyntaxException {
        // given
        InitiateUkDomesticPaymentRequest request = createInitiateUkDomesticPaymentRequest(createInitiateUkDomesticPaymentRequestDTOWithTooLongReference());

        // when
        ThrowableAssert.ThrowingCallable createPaymentCallable = () -> paymentProvider.initiateSinglePayment(request);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(createPaymentCallable)
                .withMessage("request_creation_error")
                .withCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("Remittance information contains not allowed characters. It should match ^[A-Za-z0-9\\/\\s\\.\\+\\:\\(,\\&\\')-?]{1,140}$"));
    }

    private InitiateUkDomesticPaymentRequest createInitiateUkDomesticPaymentRequest(InitiateUkDomesticPaymentRequestDTO requestDTO) throws IOException, URISyntaxException {
        return new InitiateUkDomesticPaymentRequest(requestDTO,
                "http://yolt.com/callback",
                "state",
                RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForPis(),
                new SignerMock(),
                new RestTemplateManagerMock(() -> "12345"),
                "127.0.0.1",
                authenticationMeansReference);
    }

    private InitiateUkDomesticPaymentRequestDTO createInitiateUkDomesticPaymentRequestDTOWithTooLongEndToEndIdentification() {
        return new InitiateUkDomesticPaymentRequestDTO("tooLongEndToEndIdentification1234567890",
                "GBP",
                new BigDecimal("10.00"),
                new UkAccountDTO("123123123", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Creditor", ""),
                new UkAccountDTO("321321321", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Debtor", ""),
                "SomeRandomMessage",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "SomeRandomMessage2"));
    }

    private InitiateUkDomesticPaymentRequestDTO createInitiateUkDomesticPaymentRequestDTOWithTooLongReference() {
        String tooLongReference = "AccordingToC4PO-9396ReferenceCanHasMaxLongEqualsTo140charactersSoThisReferenceMustBeLongerAccordingToC4PO-9396ReferenceCanHasMaxLongEqualsTo140charactersSoThisReferenceMustBeLonger";
        return new InitiateUkDomesticPaymentRequestDTO(UUID.randomUUID().toString().substring(6),
                "GBP",
                new BigDecimal("10.00"),
                new UkAccountDTO("123123123", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Creditor", ""),
                new UkAccountDTO("321321321", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Debtor", ""),
                "SomeRandomMessage",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, tooLongReference));
    }
}
