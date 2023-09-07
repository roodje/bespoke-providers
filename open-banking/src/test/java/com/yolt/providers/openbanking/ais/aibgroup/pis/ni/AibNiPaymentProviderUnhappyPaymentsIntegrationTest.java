package com.yolt.providers.openbanking.ais.aibgroup.pis.ni;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.aibgroup.AibGroupApp;
import com.yolt.providers.openbanking.ais.aibgroup.AibGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseSepaPaymentProvider;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.constants.OAuth.STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

/**
 * This test contains all payment flows ending with payment rejection in AIB (NI).
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
@AutoConfigureWireMock(stubs = "classpath:/stubs/aibgroup/ni/pis-3.1.1_single/unhappy-flow", httpsPort = 0, port = 0)
public class AibNiPaymentProviderUnhappyPaymentsIntegrationTest {

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
    @Qualifier("AibNiPaymentProviderV1")
    private GenericBaseSepaPaymentProvider aibNiPaymentProviderV1;

    private Stream<GenericBaseSepaPaymentProvider> getProviders() {
        return Stream.of(aibNiPaymentProviderV1);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = AibGroupSampleAuthenticationMeans.getAibGroupSampleAuthenticationMeansForPis();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d0a9b85f-9715-4d16-a33d-4323ceab5253");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResultWithStatusRejectedWhenPaymentHaveRejectedStatusInResponseDuringCreation(GenericBaseSepaPaymentProvider subject) throws JsonProcessingException {
        // given
        var paymentRequest = createPaymentRequest(createRejectedInitiateRequestDTO("ACME Inc creditor"));

        // when
        var response = subject.initiatePayment(paymentRequest);

        // then
        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58923", PaymentType.SINGLE, "null");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    //ok
    public void shouldReturnResultWithStatusRejectedWhenSubmitPaymentHaveRejectedStatusInResponse(GenericBaseSepaPaymentProvider subject) throws JsonProcessingException {
        // given
        String providerState = """
                {"consentId":"58925","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20200515101750462-522347ee-5e0\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\\"LocalInstrument\\":\\"UK.OBIE.FPS\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"EUR\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.IBAN\\",\\"Identification\\":\\"GB77BOFI60161331926819\\",\\"Name\\":\\"ACME Inc creditor\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remittance Unstructured\\",\\"Reference\\":\\"REF0123456789-0123\\"}}"}""";
        var confirmPaymentRequest = createConfirmPaymentRequest(providerState);

        // when
        var response = subject.submitPayment(confirmPaymentRequest);

        // then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58925", PaymentType.SINGLE, """
                        {"InstructionIdentification":"20200515101750462-522347ee-5e0","EndToEndIdentification":"B7F2761C","LocalInstrument":"UK.OBIE.FPS","InstructedAmount":{"Amount":"10000.00","Currency":"EUR"},"CreditorAccount":{"SchemeName":"UK.OBIE.IBAN","Identification":"GB77BOFI60161331926819","Name":"ACME Inc creditor"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");

        assertThat(response.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                });
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionForInitiateSinglePaymentWhenInvalidPaymentObjectWasReceived(GenericBaseSepaPaymentProvider subject) {
        // given
        var paymentRequest = createPaymentRequest(createRejectedInitiateRequestDTO(null));

        //when
        ThrowableAssert.ThrowingCallable createPaymentCallable = () -> subject.initiatePayment(paymentRequest);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(createPaymentCallable)
                .withMessage("request_creation_error");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnCorrectPECMetadataWhenPaymentSubmissionFailsDueToClientError(GenericBaseSepaPaymentProvider subject) {
        // given
        String providerState = """
                {"consentId":"58926","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20200515101750462-522347ee-5e0\\",\\"EndToEndIdentification\\":\\"B7F2761C400\\",\\"LocalInstrument\\":\\"UK.OBIE.FPS\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"EUR\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.IBAN\\",\\"Identification\\":\\"GB77BOFI60161331926819\\",\\"Name\\":\\"ACME Inc creditor\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remittance Unstructured\\",\\"Reference\\":\\"REF0123456789-0123\\"}}"}""";
        var confirmPaymentRequest = createConfirmPaymentRequest(providerState);

        // when
        var response = subject.submitPayment(confirmPaymentRequest);

        //then
        assertThat(response.getPaymentId()).isEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("400");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.EXECUTION_FAILED);
                }));
    }

    private SepaInitiatePaymentRequestDTO createRejectedInitiateRequestDTO(String creditorName) {
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setRemittanceInformationStructured("REF0123456789-0123");
        SepaAccountDTO creditorAccount = new SepaAccountDTO(CurrencyCode.EUR, "GB77BOFI60161331926819");
        return SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(creditorAccount)
                .endToEndIdentification("B7F2761CREJECTED")
                .creditorName(creditorName)
                .instructedAmount(new SepaAmountDTO(new BigDecimal("-10000.00")))
                .remittanceInformationUnstructured("Remittance Unstructured")
                .dynamicFields(dynamicFields)
                .build();
    }

    private SubmitPaymentRequest createConfirmPaymentRequest(String providerState) {
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

    private InitiatePaymentRequest createPaymentRequest(SepaInitiatePaymentRequestDTO requestDTO) {
        return new InitiatePaymentRequest(requestDTO,
                REDIRECT_URL,
                STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
    }
}
