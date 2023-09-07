package com.yolt.providers.openbanking.ais.hsbcgroup.pis.single;

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
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme.SORTCODEACCOUNTNUMBER;
import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all uk payment happy flows occurring in HSBC.
 * <p>
 * Covered flows:
 * - successful return of consent page url
 * - successful creation of payment
 * - successful confirmation of payment
 * - successful getting status of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = "classpath:/stubs/hsbcgroup/pis-3.1.6/single/happy-flow", httpsPort = 0, port = 0)
class HsbcGroupUkDomesticPaymentV14ProviderIntegrationTest {

    private static final String CLIENT_ID = "c54976c8-71a7-4e53-b3a5-b68260698d5e";
    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STUBBED_PAYMENT_SUBMISSION_ID = "0000f7f237ee4e6eaff0c3df18246676";
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
    @Qualifier("HsbcPaymentProviderV14")
    private GenericBasePaymentProviderV3 hsbcPaymentProviderV14;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV14")
    private GenericBasePaymentProviderV3 firstDirectPaymentProviderV14;

    private Stream<GenericBasePaymentProviderV3> getProviders() {
        return Stream.of(hsbcPaymentProviderV14,
                firstDirectPaymentProviderV14);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d10f24f4-032a-4843-bfc9-22b599c7ae2d");
        authenticationMeans = new HsbcGroupSampleAuthenticationMeansV2().getHsbcGroupSampleAuthenticationMeansForPis();
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithAuthorizationUrlForInitiateSinglePaymentWhenCorrectDataAreProvided(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO();
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
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldCreateUkDomesticPaymentWithIban(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createUkDomesticPaymentWithIban();
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
                .contains("client_id=c54976c8-71a7-4e53-b3a5-b68260698d5e")
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-test")
                .contains("request=");
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldCreateUkDomesticPaymentWithSortCodeAccountNumber(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createUkDomesticPaymentWithSortCodeAccountNumber();
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
                .contains("client_id=c54976c8-71a7-4e53-b3a5-b68260698d5e")
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-test")
                .contains("request=");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithAuthorizationUrlAndPecMetadataWithInitiationSuccessStatusForInitiateSinglePaymentWhenCorrectDataAreProvided(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO();
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
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithPaymentIdForSubmitPaymentWhenCorrectDataAreProvided(PaymentSubmissionProvider paymentProvider) throws GeneralConfirmException {
        // given
        String providerState = """
                {"consentId":"462d1a04-4g74-42d7-ba69-0bbe1e5fef68","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"8272908780568576\\",\\"Name\\":\\"Alex Mitchell\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remittance Unstructured\\",\\"Reference\\":\\"REF0123456789-0123\\"}}"}""";
        SubmitPaymentRequest request = createUkDomesticConfirmPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo(STUBBED_PAYMENT_SUBMISSION_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithPaymentIdAndPecMetadataWithCompletedStatusForSubmitPaymentWhenCorrectDataAreProvided(PaymentSubmissionProvider paymentProvider) throws GeneralConfirmException {
        // given
        String providerState = """
                {"consentId":"462d1a04-4g74-42d7-ba69-0bbe1e5fef68","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"8272908780568576\\",\\"Name\\":\\"Alex Mitchell\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remittance Unstructured\\",\\"Reference\\":\\"REF0123456789-0123\\"}}"}""";
        SubmitPaymentRequest request = createUkDomesticConfirmPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo(STUBBED_PAYMENT_SUBMISSION_ID);
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
                contains("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"331d76df48ed41229b67f062dd55e340"}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithPaymentIdAndPecMetadataWithCompletedStatusForGetStatusWhenPaymentIdIsProvidedInRequest(PaymentSubmissionProvider paymentProvider) {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(true);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEqualTo("e23f5d5cd08d44c3993243ad3f19d56e");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedCreditSettlementCompleted");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO() {
        return new InitiateUkDomesticPaymentRequestDTO(
                "35B64F93",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                new UkAccountDTO(VALID_CREDITOR_NUMBER, SORTCODEACCOUNTNUMBER, "Jordan Bell", null),
                new UkAccountDTO(VALID_DEBTOR_NUMBER, SORTCODEACCOUNTNUMBER, "Alex Mitchell", null),
                "Remittance Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "REF0123456789-0123")
        );
    }

    private InitiateUkDomesticPaymentRequestDTO createUkDomesticPaymentWithSortCodeAccountNumber() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
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

    private InitiateUkDomesticPaymentRequestDTO createUkDomesticPaymentWithIban() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.IBAN, "Alex Mitchell", null);
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
                "https://www.yolt.com/callback-test/payments/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0#code=" + authorizationCode,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private GetStatusRequest creteUkDomesticGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequest(createUkProviderState(new UkProviderState("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, "")),
                withPaymentId ? "e23f5d5cd08d44c3993243ad3f19d56e" : null,
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