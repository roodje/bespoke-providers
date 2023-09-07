package com.yolt.providers.openbanking.ais.nationwide.pis.v12;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.nationwide.NationwideApp;
import com.yolt.providers.openbanking.ais.nationwide.NationwideSampleAuthenticationMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all UK domestic payment happy flows occurring in Nationwide.
 * <p>
 * Disclaimer: Nationwide is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - successful creation of payment and consent page generation with IBANs
 * - successful creation of payment and consent page generation with SortCodeAccountNumbers
 * - successful confirmation of payment with IBANs
 * - successful getting status of payment
 * <p>
 */
@SpringBootTest(classes = {NationwideApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/nationwide/pis-3.1.2/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("nationwide")
public class NationwideUkDomesticPaymentPaymentProviderV12HappyFlowIntegrationTest {

    private static final String STUBBED_PAYMENT_SUBMISSION_ID = "21c2242a-2efb-2fdb-b24e-27c6b0f629b5";
    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";

    private RestTemplateManagerMock restTemplateManagerMock;
    private String requestTraceId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("NationwidePaymentProviderV12")
    private GenericBasePaymentProviderV2 paymentProvider;

    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private Signer signer = new SignerMock();

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new NationwideSampleAuthenticationMeans().getAuthenticationMeans();
        requestTraceId = "12345";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
    }

    @Test
    void shouldReturnResponseWithAuthorizationUrlAndPecMetadataForInitiateSinglePaymentWhenCorrectDataWithIbanAreProvided() {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO(AccountIdentifierScheme.IBAN);
        InitiateUkDomesticPaymentRequest paymentRequest = createInitiateUkDomesticPaymentRequest(requestDTO);

        // when
        InitiateUkDomesticPaymentResponseDTO paymentResponse = paymentProvider.initiateSinglePayment(paymentRequest);

        // then
        assertThat(paymentResponse.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=someClientId")
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-test")
                .contains("request=");
        assertThat(paymentResponse.getProviderState()).isNotEmpty();
        assertThat(paymentResponse.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldReturnResponseWithAuthorizationUrlAndPecMetadataForInitiateSinglePaymentWhenCorrectDataWithSortCodeAccountNumberAreProvided() {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO(AccountIdentifierScheme.SORTCODEACCOUNTNUMBER);
        InitiateUkDomesticPaymentRequest paymentRequest = createInitiateUkDomesticPaymentRequest(requestDTO);

        // when
        InitiateUkDomesticPaymentResponseDTO paymentResponse = paymentProvider.initiateSinglePayment(paymentRequest);

        // then
        assertThat(paymentResponse.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=someClientId")
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-test")
                .contains("request=");
        assertThat(paymentResponse.getProviderState()).isNotEmpty();
        assertThat(paymentResponse.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldReturnResponseWithPaymentIdAndPecMetadataWithAcceptedStatusForSubmitPaymentWhenCorrectDataAreProvided() {
        // given
        String providerState = "{\"consentId\":\"DIP_1_8_4c645ed0-3451-14eb-aa49-e342fc814dbd\",\"paymentType\":\"SINGLE\",\"openBankingPayment\":\"{\\\"InstructionIdentification\\\":\\\"20201203108401220-50afa69f-ad6\\\",\\\"EndToEndIdentification\\\":\\\"FEF32557\\\",\\\"InstructedAmount\\\":{\\\"Amount\\\":\\\"100.00\\\",\\\"Currency\\\":\\\"GBP\\\"},\\\"DebtorAccount\\\":{\\\"SchemeName\\\":\\\"UK.OBIE.IBAN\\\",\\\"Identification\\\":\\\"8272908780568576\\\",\\\"Name\\\":\\\"Alex Mitchell\\\"},\\\"CreditorAccount\\\":{\\\"SchemeName\\\":\\\"UK.OBIE.IBAN\\\",\\\"Identification\\\":\\\"1802968485593088\\\",\\\"Name\\\":\\\"Jordan Bell\\\"},\\\"RemittanceInformation\\\":{\\\"Unstructured\\\":\\\"Payment\\\"}}\"}";
        SubmitPaymentRequest request = createConfirmPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo(STUBBED_PAYMENT_SUBMISSION_ID);
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Pending");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @Test
    void shouldReturnResponseWithPaymentIdAndPecMetadataWithCompletedStatusForGetStatusWhenPaymentIdIsProvidedInRequest() {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(true);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEqualTo("21c2242a-2efb-2fdb-b24e-27c6b0f629b5");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedCreditSettlementCompleted");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @Test
    void shouldReturnResponseWithConsentIdAndPecMetadataWithInitiationSuccessStatusForGetStatusWhenPaymentIdIsNotProvidedInRequest() throws JsonProcessingException {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(false);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("DIP_1_8_4c645ed0-3451-14eb-aa49-e342fc814dbd", PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"DIP_1_8_4c645ed0-3451-14eb-aa49-e342fc814dbd"}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO(AccountIdentifierScheme scheme) {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", scheme, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", scheme, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "FEF32557",
                CurrencyCode.GBP.toString(),
                new BigDecimal("100.00"),
                creditorAccount,
                debtorAccount,
                "Payment",
                new HashMap<>()
        );
    }

    private InitiateUkDomesticPaymentRequest createInitiateUkDomesticPaymentRequest(InitiateUkDomesticPaymentRequestDTO requestDTO) {
        return new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private GetStatusRequest creteUkDomesticGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequest(withPaymentId ? null : createUkProviderState(new UkProviderState("DIP_1_8_4c645ed0-3451-14eb-aa49-e342fc814dbd", PaymentType.SINGLE, null)),
                withPaymentId ? "21c2242a-2efb-2fdb-b24e-27c6b0f629b5" : null,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                null,
                authenticationMeansReference);
    }

    private static String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private SubmitPaymentRequest createConfirmPaymentRequest(String providerState) {
        String authorizationCode = "asF0cFXAI6bacqdrGRArpwrR6fK6yq";
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payments/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0#code=" + authorizationCode,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }
}