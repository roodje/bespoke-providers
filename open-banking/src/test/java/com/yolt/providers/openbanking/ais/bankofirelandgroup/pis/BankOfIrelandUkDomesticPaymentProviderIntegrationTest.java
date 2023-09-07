package com.yolt.providers.openbanking.ais.bankofirelandgroup.pis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.exception.GeneralConfirmException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandGroupApp;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme.SORTCODEACCOUNTNUMBER;
import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all uk payment happy flows occurring in Bank Of Ireland.
 * <p>
 * Covered flows:
 * - successful return of consent page url
 * - successful creation of payment
 * - successful confirmation of payment
 * - successful getting status of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BankOfIrelandGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bankofireland")
@AutoConfigureWireMock(stubs = "classpath:/stubs/bankofireland/pis-3.0.0/single/happy-flow", httpsPort = 0, port = 0)
class BankOfIrelandUkDomesticPaymentProviderIntegrationTest {

    private static final String CLIENT_ID = "someClientId";
    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String CONSENT_ID = "462d1a04-4g74-42d7-ba69-0bbe1e5fef68";
    private static final String PAYMENT_ID = "e23f5d5cd08d44c3993243ad3f19d56e";
    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final String VALID_CREDITOR_NUMBER = "1802968485593088";
    private static final String VALID_DEBTOR_NUMBER = "8272908780568576";

    private final AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("BankOfIrelandPaymentProviderV1")
    private GenericBasePaymentProviderV3 bankOfIrelandPaymentProviderV1;

    private Stream<GenericBasePaymentProviderV3> getProviders() {
        return Stream.of(bankOfIrelandPaymentProviderV1);
    }

    @BeforeEach
    void beforeEach() {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d10f24f4-032a-4843-bfc9-22b599c7ae2d");
        authenticationMeans = BankOfIrelandSampleTypedAuthMeans.getSampleAuthMeans();
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithAuthorizationUrlForInitiateSinglePaymentWhenCorrectDataAreProvided(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException, JsonProcessingException {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO(false);
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=" + CLIENT_ID)
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-test")
                .contains("request=");

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains(CONSENT_ID, PaymentType.SINGLE, """
                        {"InstructionIdentification":"20200515101750462-522347ee-5e0","EndToEndIdentification":"35B64F93","InstructedAmount":{"Amount":"0.01","Currency":"GBP"},"CreditorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"1802968485593088","Name":"Jordan Bell"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithAuthorizationUrlForInitiateSinglePaymentWhenCorrectDataAreProvidedAndContainsDebtor(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException, JsonProcessingException {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO(true);
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=" + CLIENT_ID)
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-test")
                .contains("request=");
        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains(CONSENT_ID, PaymentType.SINGLE, """
                        {"InstructionIdentification":"20200515101750462-522347ee-5e0","EndToEndIdentification":"35B64F93","InstructedAmount":{"Amount":"0.01","Currency":"GBP"},"DebtorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"8272908780568576","Name":"Alex Mitchell"},"CreditorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"1802968485593088","Name":"Jordan Bell"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldCreateUkDomesticPaymentWithIban(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException, JsonProcessingException {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createUkDomesticPaymentWithIban(false);
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=" + CLIENT_ID)
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-test")
                .contains("request=");
        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains(CONSENT_ID, PaymentType.SINGLE, """
                        {"InstructionIdentification":"20200515101750462-522347ee-5e0","EndToEndIdentification":"35B64F93","InstructedAmount":{"Amount":"0.01","Currency":"GBP"},"CreditorAccount":{"SchemeName":"UK.OBIE.IBAN","Identification":"1802968485593088","Name":"Jordan Bell"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }


    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithPaymentIdForSubmitPaymentWhenCorrectDataAreProvided(PaymentSubmissionProvider paymentProvider) throws GeneralConfirmException, JsonProcessingException {
        // given
        String providerState = """
                {"consentId":"462d1a04-4g74-42d7-ba69-0bbe1e5fef68","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20200515101750462-522347ee-5e0\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remittance Unstructured\\",\\"Reference\\":\\"REF0123456789-0123\\"}}"}""";
        SubmitPaymentRequest request = createUkDomesticConfirmPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo(PAYMENT_ID);
        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains(CONSENT_ID, PaymentType.SINGLE, """
                        {"InstructionIdentification":"20200515101750462-522347ee-5e0","EndToEndIdentification":"35B64F93","InstructedAmount":{"Amount":"0.01","Currency":"GBP"},"CreditorAccount":{"SchemeName":"UK.OBIE.SortCodeAccountNumber","Identification":"1802968485593088","Name":"Jordan Bell"},"CreditorPostalAddress":{},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementCompleted");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }


    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithConsentIdAndPecMetadataWithInitiationSuccessStatusForGetStatusWhenPaymentIdIsNotProvidedInRequest(PaymentSubmissionProvider paymentProvider) throws JsonProcessingException {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(false);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEmpty();
        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains(CONSENT_ID, PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"462d1a04-4g74-42d7-ba69-0bbe1e5fef68"}""");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithPaymentIdAndPecMetadataWithCompletedStatusForGetStatusWhenPaymentIdIsProvidedInRequest(PaymentSubmissionProvider paymentProvider) throws JsonProcessingException {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(true);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEqualTo(PAYMENT_ID);
        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains(CONSENT_ID, PaymentType.SINGLE, """
                        {"Status":"AcceptedCreditSettlementCompleted","resourceId":"e23f5d5cd08d44c3993243ad3f19d56e"}""");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedCreditSettlementCompleted");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO(boolean withDebtor) {
        return new InitiateUkDomesticPaymentRequestDTO(
                "35B64F93",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                new UkAccountDTO(VALID_CREDITOR_NUMBER, SORTCODEACCOUNTNUMBER, "Jordan Bell", null),
                withDebtor ? new UkAccountDTO(VALID_DEBTOR_NUMBER, SORTCODEACCOUNTNUMBER, "Alex Mitchell", null) : null,
                "Remittance Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "REF0123456789-0123")
        );
    }

    private InitiateUkDomesticPaymentRequestDTO createUkDomesticPaymentWithIban(boolean withDebtor) {
        UkAccountDTO debtorAccount = withDebtor ? new UkAccountDTO("8272908780568576", AccountIdentifierScheme.IBAN, "Alex Mitchell", null) : null;
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.IBAN, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "35B64F93",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                debtorAccount,
                "Remittance Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "REF0123456789-0123")
        );
    }

    private SubmitPaymentRequest createUkDomesticConfirmPaymentRequest(String providerState) {
        String authorizationCode = "iVuATHQHLIjtGjeuxtBj6Gfnd8o";
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                "https://yolt.com/callback-test?code=" + authorizationCode,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private GetStatusRequest creteUkDomesticGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequest(createUkProviderState(new UkProviderState(CONSENT_ID, PaymentType.SINGLE, "")),
                withPaymentId ? PAYMENT_ID : null,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                null,
                authenticationMeansReference);
    }

    private String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return objectMapper.writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            System.out.println(e.getStackTrace());
            return null;
        }
    }
}