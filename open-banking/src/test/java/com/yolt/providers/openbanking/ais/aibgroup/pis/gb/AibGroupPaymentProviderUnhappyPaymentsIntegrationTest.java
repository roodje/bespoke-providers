package com.yolt.providers.openbanking.ais.aibgroup.pis.gb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.aibgroup.AibGroupApp;
import com.yolt.providers.openbanking.ais.aibgroup.AibGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
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

import static com.yolt.providers.common.constants.OAuth.STATE;
import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

/**
 * This test contains all payment flows ending with payment rejection in Barclays.
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
@SpringBootTest(classes = {AibGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("aib")
@AutoConfigureWireMock(stubs = "classpath:/stubs/aibgroup/gb/pis-3.1.1_single/unhappy-flow", httpsPort = 0, port = 0)
public class AibGroupPaymentProviderUnhappyPaymentsIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545";
    private static final String AUTHORIZATION_CODE = "?code=test_code";
    private static final Signer SIGNER = new SignerMock();
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("AibPaymentProviderV1")
    private GenericBasePaymentProviderV3 aibgbPaymentProviderV1;

    private Stream<GenericBasePaymentProviderV3> getProviders() {
        return Stream.of(aibgbPaymentProviderV1);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = AibGroupSampleAuthenticationMeans.getAibGroupSampleAuthenticationMeansForPis();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d0a9b85f-9715-4d16-a33d-4323ceab5253");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResultWithStatusRejectedWhenPaymentHaveRejectedStatusInResponseDuringCreation(GenericBasePaymentProviderV3 subject) throws JsonProcessingException {
        // given
        InitiateUkDomesticPaymentRequest paymentRequest = createUkDomesticPaymentRequest(createRejectedInitiateRequestDTO("ACME Inc creditor"));

        // when
        InitiateUkDomesticPaymentResponseDTO result = subject.initiateSinglePayment(paymentRequest);

        // then
        UkProviderState state = objectMapper.readValue(result.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58923", PaymentType.SINGLE, "null");

        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    //ok
    public void shouldReturnResultWithStatusRejectedWhenSubmitPaymentHaveRejectedStatusInResponse(GenericBasePaymentProviderV3 subject) throws JsonProcessingException {
        // given
        String providerState = """
                {"consentId":"58925","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20200515101750462-522347ee-5e0\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\\"LocalInstrument\\":\\"UK.OBIE.FPS\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"98765432104322\\",\\"Name\\":\\"ACME Inc creditor\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remittance Unstructured\\",\\"Reference\\":\\"REF0123456789-0123\\"}}"}""";
        SubmitPaymentRequest confirmPaymentRequest = createUkDomesticSubmitPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO result = subject.submitPayment(confirmPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(result.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58925", PaymentType.SINGLE, """
                        {"InstructionIdentification":"20200515101750462-522347ee-5e0","EndToEndIdentification":"B7F2761C","LocalInstrument":"UK.OBIE.FPS","InstructedAmount":{"Amount":"10000.00","Currency":"GBP"},"CreditorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"98765432104322","Name":"ACME Inc creditor"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");

        assertThat(result.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                });
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionForInitiateSinglePaymentWhenInvalidPaymentObjectWasReceived(GenericBasePaymentProviderV3 subject) {
        // given
        // when
        ThrowableAssert.ThrowingCallable createPaymentCallable = () -> subject.initiateSinglePayment(null);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(createPaymentCallable)
                .withMessage("request_creation_error");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnCorrectPECMetadataWhenPaymentSubmissionFailsDueToClientError(GenericBasePaymentProviderV3 subject) {
        // given
        String providerState = """
                {"consentId":"58926","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20200515101750462-522347ee-5e0\\",\\"EndToEndIdentification\\":\\"B7F2761C400\\",\\"LocalInstrument\\":\\"UK.OBIE.FPS\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"98765432104322\\",\\"Name\\":\\"ACME Inc creditor\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remittance Unstructured\\",\\"Reference\\":\\"REF0123456789-0123\\"}}"}""";
        SubmitPaymentRequest confirmPaymentRequest = createUkDomesticSubmitPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO result = subject.submitPayment(confirmPaymentRequest);

        //then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("400");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.EXECUTION_FAILED);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createRejectedInitiateRequestDTO(String creditorName) {
        UkAccountDTO creditorAccount = new UkAccountDTO("98765432104322",
                AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                creditorName,
                null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "B7F2761CREJECTED",
                CurrencyCode.GBP.toString(),
                new BigDecimal("-10000.00"),
                creditorAccount,
                null,
                "Remittance Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "REF0123456789-0123")
        );
    }

    private SubmitPaymentRequest createUkDomesticSubmitPaymentRequest(String providerState) {
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                REDIRECT_URL + AUTHORIZATION_CODE,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private InitiateUkDomesticPaymentRequest createUkDomesticPaymentRequest(InitiateUkDomesticPaymentRequestDTO requestDTO) {
        return new InitiateUkDomesticPaymentRequest(requestDTO,
                REDIRECT_URL,
                STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
    }
}