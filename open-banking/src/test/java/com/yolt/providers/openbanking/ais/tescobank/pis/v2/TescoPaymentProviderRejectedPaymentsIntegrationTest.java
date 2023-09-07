package com.yolt.providers.openbanking.ais.tescobank.pis.v2;

import com.yolt.providers.common.cryptography.Signer;
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
import com.yolt.providers.openbanking.ais.tescobank.TescoBankApp;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleTypedAuthenticationMeansV2;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
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
 * This test contains all payment flows ending with payment rejection in provider.
 * <p>
 * Covered flows:
 * - when creating payment ends with rejection status we throw CreationFailedException
 * - when created payment is incorrect we throw CreationFailedException
 * - when confirming payment ends with rejection status we throw ConfirmationFailedException
 * - when authorization fails with Access Denied we end with PaymentCancelledException
 * - when authorization fails with server error in url we end with ConfirmationFailedException
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tescobank/pis-3.1/rejected-payment", httpsPort = 0, port = 0)
@ActiveProfiles("tescobank")
public class TescoPaymentProviderRejectedPaymentsIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String AUTHORIZATION_CODE = "?code=fakeAuthCode";
    private static final Signer SIGNER = new SignerMock();
    private static final String REQUEST_TRACE_ID = UUID.randomUUID().toString();
    private static final UUID CLIENT_ID = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final AuthenticationMeansReference AUTHENTICATION_MEANS_REFERENCE = new AuthenticationMeansReference(CLIENT_ID, CLIENT_REDIRECT_URL_ID);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("TescoBankPaymentProviderV5")
    private GenericBasePaymentProviderV2 tescoBankPaymentProviderV5;

    private Stream<GenericBasePaymentProviderV2> getProviders() {
        return Stream.of(tescoBankPaymentProviderV5);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = TescoSampleTypedAuthenticationMeansV2.getTypedAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> REQUEST_TRACE_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionWithIllegalArgumentExceptionAsCauseForInitiateSinglePaymentWhenCreatedInvalidUkPayment(UkDomesticPaymentProvider provider) {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createdUkPaymentRequest("B7F2761C", "10000.00", "Too looooooooong remittance information");
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "test",
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE
        );

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> provider.initiateSinglePayment(paymentRequest);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(throwingCallable)
                .withMessage("request_creation_error")
                .withCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("Reference in payment is too long (39), maximum allowed for Tesco is 18 characters"));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithEmptyLoginUrlAndEmptyProviderStateAndRejectedStatusInPecMetadataForInitiateSinglePaymentWhenRejectedStatusInResponse(UkDomesticPaymentProvider provider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createdUkPaymentRequest("B7F2761C", "-10000.00", null);
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "test",
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = provider.initiateSinglePayment(paymentRequest);

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
    @MethodSource("getProviders")
    public void shouldReturnResponseWithEmptyLoginUrlAndEmptyProviderStateAndRejectedStatusInPecMetadataForInitiateSinglePaymentWhenBadRequestFromBank(UkDomesticPaymentProvider provider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createdUkPaymentRequest("B7F2761D", "10000.00", null);
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "test",
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = provider.initiateSinglePayment(paymentRequest);

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
    @MethodSource("getProviders")
    public void shouldReturnResponseWithEmptyPaymentIdAndExecutionFailedStatusInPecMetadataForSubmitPaymentWhenStatusRejectedInResponse(PaymentSubmissionProvider provider) throws ConfirmationFailedException {
        // given
        String state = """
                {"consentId":"PAYMENT_SUBMISSION_ID","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20201202002028103-4b1t742n-102\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"45654645435345\\",\\"Name\\":\\"Jordan Bell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"20581634112471\\",\\"Name\\":\\"Robin Hood\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(state,
                authenticationMeans,
                REDIRECT_URL + AUTHORIZATION_CODE,
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE);

        // when
        PaymentStatusResponseDTO response = provider.submitPayment(request);

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
    @MethodSource("getProviders")
    public void shouldReturnResponseWithEmptyPaymentIdAndRejectedStatusInPecMetadataForSubmitPaymentWhenBadRequestFromBank(PaymentSubmissionProvider provider) throws ConfirmationFailedException {
        // given
        String state = """
                {"consentId":"TESCOBANK-P-PAYMENT_ID","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20201202002028103-4b1t742n-102\\",\\"EndToEndIdentification\\":\\"B7F2761D\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"45654645435345\\",\\"Name\\":\\"Jordan Bell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"20581634112471\\",\\"Name\\":\\"Robin Hood\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(state,
                authenticationMeans,
                REDIRECT_URL + AUTHORIZATION_CODE,
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE);

        // when
        PaymentStatusResponseDTO response = provider.submitPayment(request);

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
    @MethodSource("getProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionWithPaymentCancelledExceptionAsCaseForSubmitPaymentWhenAccessDeniedErrorInRedirectUrl(PaymentSubmissionProvider provider) {
        // given
        String state = """
                {"consentId":"PAYMENT_SUBMISSION_ID","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20201202002028103-4b1t742n-102\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"45654645435345\\",\\"Name\\":\\"Jordan Bell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"20581634112471\\",\\"Name\\":\\"Robin Hood\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(state,
                authenticationMeans,
                REDIRECT_URL + "?error=access_denied",
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> provider.submitPayment(request);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(throwingCallable)
                .withMessage("submit_preparation_error")
                .withCauseInstanceOf(PaymentCancelledException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("Got error in redirect URL: access_denied"));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionWithConfirmationFailedExceptionAsCauseForSubmitPaymentWhenUnknownErrorInRedirectUrl(PaymentSubmissionProvider provider) {
        // given
        String state = """
                {"consentId":"PAYMENT_SUBMISSION_ID","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20201202002028103-4b1t742n-102\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"45654645435345\\",\\"Name\\":\\"Jordan Bell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"20581634112471\\",\\"Name\\":\\"Robin Hood\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\"}}"}""";
        SubmitPaymentRequest request = new SubmitPaymentRequest(state,
                authenticationMeans,
                REDIRECT_URL + "?error=other_error",
                SIGNER,
                restTemplateManagerMock,
                null,
                AUTHENTICATION_MEANS_REFERENCE);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> provider.submitPayment(request);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(throwingCallable)
                .withMessage("submit_preparation_error")
                .withCauseInstanceOf(GenericPaymentRequestInvocationException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("com.yolt.providers.common.exception.ConfirmationFailedException: Got error in redirect URL: other_error"));
    }

    private InitiateUkDomesticPaymentRequestDTO createdUkPaymentRequest(String endToEndIdentification, String amount, String remittanceReference) {
        return new InitiateUkDomesticPaymentRequestDTO(
                endToEndIdentification,
                CurrencyCode.GBP.toString(),
                new BigDecimal(amount),
                new UkAccountDTO("20581634112471", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Robin Hood", null),
                new UkAccountDTO("45654645435345", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null),
                "Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, remittanceReference)
        );
    }
}