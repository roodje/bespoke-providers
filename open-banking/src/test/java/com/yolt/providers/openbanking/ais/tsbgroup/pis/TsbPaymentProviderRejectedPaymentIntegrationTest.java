package com.yolt.providers.openbanking.ais.tsbgroup.pis;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.exception.PaymentCancelledException;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.GenericPaymentRequestInvocationException;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupApp;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupSampleTypedAuthenticationMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test contains all payment rejection flows occuring in TSB.
 * <p>
 * Disclaimer: as all providers in TSB group are the same from code and stubs perspective (then only difference is configuration)
 * we are running parametirized tests for testing, but this covers all providers from TSB group.
 * <p>
 * Covered flows:
 * - proper handling of "Rejected" status after creation of payments
 * - proper handling of "Rejected" status after creation of payments for UK domestic payments
 * - proper handling of "Rejected" status after confirmation of payments
 * - proper handling of "Rejected" status after confirmation of payments for UK domestic payments
 * - handling of payment cancellation
 * - handling of payment cancellation for UK domestic payments
 * - handling server error in redirect url during confirmation
 * - handling server error in redirect url during confirmation for UK domestic payments
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TsbGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("tsbgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/tsbgroup/pis/ob_3.1.1/payment-rejection", httpsPort = 0, port = 0)
public class TsbPaymentProviderRejectedPaymentIntegrationTest {

    private static final UUID CLIENT_ID = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");

    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("TsbPaymentProviderV5")
    private GenericBasePaymentProviderV2 tsbPaymentProviderV5;
    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID, CLIENT_REDIRECT_URL_ID);

    private Stream<GenericBasePaymentProviderV2> getTsbPaymentProviders() {
        return Stream.of(tsbPaymentProviderV5);
    }

    @BeforeAll
    public static void beforeAll() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "87da2798-f7e2-4823-80c1-3c03344b8f13");

        final ProviderAccountNumberDTO creditorAccount = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER, "12345678901234");
        creditorAccount.setHolderName("P. Jantje");

        ProviderAccountNumberDTO debtor = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER, "3238619340865536");

        debtor.setHolderName("Owen McDaniel");

        authenticationMeans = new TsbGroupSampleTypedAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldReturnResponseWithEmptyLoginUrlAndEmptyStateAndInitiationErrorStatusInPecMetadataForInitiateSinglePaymentWhenDomesticUkPaymentPaymentHaveRejectedStatusInResponse(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        UkAccountDTO creditorAccount = new UkAccountDTO("12345678901234",
                AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                "P. Jantje",
                null);
        InitiateUkDomesticPaymentRequestDTO requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                "35B64F93",
                String.valueOf(CurrencyCode.GBP),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                "SomeRandomMessage",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "SomeRandomMessage2")
        );

        String state = UUID.randomUUID().toString();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                "https://www.yolt.com/callback/payment",
                state,
                authenticationMeans,
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).isEmpty();
        assertThat(response.getProviderState()).isEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            {"Data":{"Status":"Rejected"}}""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldReturnResponseWithEmptyLoginUrlAndEmptyStateAndInitiationErrorStatusInPecMetadataForInitiateSinglePaymentWhenBadRequestFromBankReceived(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        UkAccountDTO creditorAccount = new UkAccountDTO("12345678901234",
                AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                "P. Jantje",
                null);
        InitiateUkDomesticPaymentRequestDTO requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                "35B64F94",
                String.valueOf(CurrencyCode.GBP),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                "SomeRandomMessage",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "SomeRandomMessage2")
        );

        String state = UUID.randomUUID().toString();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                "https://www.yolt.com/callback/payment",
                state,
                authenticationMeans,
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).isEmpty();
        assertThat(response.getProviderState()).isEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("400 BadRequest");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            [{"ErrorCode":"UK.OBIE.Field.Missing","Message":"End to end identification is missing","Path":"Data.Initiation.InstructionIdentification","Url":"<url to the api reference for Payment Inititaion API>"},{"ErrorCode":"UK.OBIE.Unsupported.Scheme","Message":"Scheme name supplied is not supported","Path":"Data.Initiation.CreditorAccount.SchemeName","Url":"<url to the online documentation referring supported scheme names>"}]""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldReturnResponseWithEmptyPaymentIdAndUnknownStatusInPecMetadataForSubmitPaymentWhenUkDomesticPaymentStatusIsRejected(PaymentSubmissionProvider paymentProvider) throws ConfirmationFailedException {
        // given
        String providerState = """
                {"consentId":"bec2bc664f984571b5a20ea666a7d0c1","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"12345678901234\\",\\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"SomeRandomMessage\\",\\"Reference\\":\\"SomeRandomMessage2\\"},\\"Risk\\":{\\"PaymentContextCode\\":\\"PartyToParty\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payment#code=fakeAuthCode",
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference);


        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            {"Data":{"Status":"Rejected"}}""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldReturnResponseWithEmptyPaymentIdAndExecutionFailedStatusInPecMetadataForSubmitPaymentWhenBadRequestReceivedFromBank(PaymentSubmissionProvider paymentProvider) throws ConfirmationFailedException {
        // given
        String providerState = """
                {"consentId":"bec2bc664f984571b5a20ea666a7d0c1","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F94\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"12345678901234\\",\\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"SomeRandomMessage\\",\\"Reference\\":\\"SomeRandomMessage2\\"},\\"Risk\\":{\\"PaymentContextCode\\":\\"PartyToParty\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payment#code=fakeAuthCode",
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference);


        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("400 BadRequest");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            [{"ErrorCode":"UK.OBIE.Field.Missing","Message":"Instructed amount does not match","Path":"Data.Initiation.InstructedAmount.Amount","Url":"<url to the api reference for Payment Inititaion API>"}]""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.EXECUTION_FAILED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionWithPaymentCancelledExceptionAsCauseForSubmitPaymentWhenUkDomesticPaymentsIsCancelledByUser(PaymentSubmissionProvider paymentProvider) {
        // given
        String providerState = """
                {"consentId":"bec2bc664f984571b5a20ea666a7d0c1","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"12345678901234\\",\\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"SomeRandomMessage\\",\\"Reference\\":\\"SomeRandomMessage2\\"},\\"Risk\\":{\\"PaymentContextCode\\":\\"PartyToParty\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payment/71b4ad6b-d620-4049-9f09-f9fd9110bd15#state=1d39c2ab-f3b2-4bc7-a37f-72c26219e38a&error=access_denied",
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference);


        // when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> paymentProvider.submitPayment(request);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(throwingCallable)
                .withMessage("submit_preparation_error")
                .withCauseInstanceOf(PaymentCancelledException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("Got error in redirect URL: access_denied"));
    }

    @ParameterizedTest
    @MethodSource("getTsbPaymentProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionWithConfirmationFailedExceptionAsCauseForSubmitPaymentWhenForUkDomesticPaymentServerErrorInRedirectUrl(PaymentSubmissionProvider paymentProvider) {
        // given
        String providerState = """
                {"consentId":"bec2bc664f984571b5a20ea666a7d0c1","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"12345678901234\\",\\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"SomeRandomMessage\\",\\"Reference\\":\\"SomeRandomMessage2\\"},\\"Risk\\":{\\"PaymentContextCode\\":\\"PartyToParty\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payment/71b4ad6b-d620-4049-9f09-f9fd9110bd15#state=1d39c2ab-f3b2-4bc7-a37f-72c26219e38a&error=server_error",
                new SignerMock(),
                restTemplateManagerMock,
                null,
                authenticationMeansReference);


        // when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> paymentProvider.submitPayment(request);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(throwingCallable)
                .withMessage("submit_preparation_error")
                .withCauseInstanceOf(GenericPaymentRequestInvocationException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("com.yolt.providers.common.exception.ConfirmationFailedException: Got error in redirect URL: server_error"));
    }
}
