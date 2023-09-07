package com.yolt.providers.openbanking.ais.danske.pis;

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
import com.yolt.providers.openbanking.ais.TestConfiguration;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.danske.DanskeApp;
import com.yolt.providers.openbanking.ais.danske.DanskeBankSampleTypedAuthenticationMeansV7;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(classes = {DanskeApp.class, TestConfiguration.class, OpenbankingConfiguration.class}, webEnvironment = NONE)
@ActiveProfiles("danske")
@AutoConfigureWireMock(stubs = "classpath:/stubs/danske/pis-3.1.8/happy-flow", httpsPort = 0, port = 0)
class DanskePaymentProviderHappyFlowIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback/3651edaa-d36e-48cb-8cc3-94bb1fbe8f76";
    private static final String AUTH_CODE_PARAM = "?code=authorization_code";
    private static final String STUBBED_PAYMENT_AUTHORIZE_URL = "https://localhost:(.*)/authorize\\?response_type=code\\+id_token&client_id=(.*)&state=(.*)&scope=openid\\+payments&nonce=(.*)&redirect_uri=(.*)&request=(.*)";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STATE = "somestate";
    private static final UUID CLIENT_ID = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final Map<String, BasicAuthenticationMean> AUTHENTICATION_MEANS = new DanskeBankSampleTypedAuthenticationMeansV7().getAuthenticationMeans();
    private static final RestTemplateManagerMock REST_TEMPLATE_MANAGER_MOCK = new RestTemplateManagerMock(() -> "3da42a4b-5e10-4807-9087-d90a8f66d997");
    private static final AuthenticationMeansReference AUTH_MEANS_REFERENCE = new AuthenticationMeansReference(CLIENT_ID, CLIENT_REDIRECT_URL_ID);
    private static final Signer SIGNER = new SignerMock();
    private static final String PROVIDER_STATE = """
            {"consentId":"f2f36954-2ef9-4c1a-999d-b8d1bbb64f4f","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20200515101750462-522347ee-5e0\\",\\"EndToEndIdentification\\":\\"E2EDN5K3\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"8272908780568576\\",\\"Name\\":\\"Alex Mitchell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remi Unstr\\",\\"Reference\\":\\"REF1122336789-4321\\"}}"}""";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("DanskePaymentProviderV1")
    private GenericBasePaymentProviderV3 paymentProvider;

    @Test
    void shouldCreatePaymentConsent() {
        // given
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                createSampleInitiateRequestDTO(),
                REDIRECT_URL,
                STATE,
                AUTHENTICATION_MEANS,
                SIGNER,
                REST_TEMPLATE_MANAGER_MOCK,
                PSU_IP_ADDRESS,
                AUTH_MEANS_REFERENCE);
        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(paymentRequest);
        // then
        assertThat(response.getLoginUrl()).matches(STUBBED_PAYMENT_AUTHORIZE_URL);
        assertThat(response.getProviderState()).isEqualTo(PROVIDER_STATE);
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldCreatePaymentConsentWithoutDebtor() {
        // given
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                createSampleInitiateRequestDTOWithoutDebtor(),
                REDIRECT_URL,
                STATE,
                AUTHENTICATION_MEANS,
                SIGNER,
                REST_TEMPLATE_MANAGER_MOCK,
                PSU_IP_ADDRESS,
                AUTH_MEANS_REFERENCE);
        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(paymentRequest);

        // then
        assertThat(response.getLoginUrl()).matches(STUBBED_PAYMENT_AUTHORIZE_URL);
        String providerState = """
                {"consentId":"f2f36954-2ef9-4c1a-999d-b8d1bbb64f4f","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20200515101750462-522347ee-5e0\\",\\"EndToEndIdentification\\":\\"E2EDN5K3\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remi Unstr\\",\\"Reference\\":\\"REF1122336789-4321\\"}}"}""";
        assertThat(response.getProviderState()).isEqualTo(providerState);
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldSubmitPayment() {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequest(PROVIDER_STATE,
                AUTHENTICATION_MEANS,
                REDIRECT_URL + AUTH_CODE_PARAM,
                SIGNER,
                REST_TEMPLATE_MANAGER_MOCK,
                PSU_IP_ADDRESS,
                AUTH_MEANS_REFERENCE);
        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo("0000d1d31c8a99bebff069dff194611c");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementCompleted");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @Test
    void shouldGetPaymentStatusWhenPaymentIdIsProvided() {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(true);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentId()).isEqualTo("0000d1d31c8a99bebff069dff194611c");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementCompleted");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @Test
    void shouldGetPaymentStatusWhenPaymentIdIsNotProvided() throws JsonProcessingException {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(false);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("f2f36954-2ef9-4c1a-999d-b8d1bbb64f4f", PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"f2f36954-2ef9-4c1a-999d-b8d1bbb64f4f"}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTOWithoutDebtor() {
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088",
                AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                "Jordan Bell",
                null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "E2EDN5K3",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                "Remi Unstr",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "REF1122336789-4321")
        );
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576",
                AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                "Alex Mitchell",
                null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088",
                AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                "Jordan Bell",
                null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "E2EDN5K3",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                debtorAccount,
                "Remi Unstr",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "REF1122336789-4321")
        );
    }

    private GetStatusRequest creteUkDomesticGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequest(createUkProviderState(new UkProviderState("f2f36954-2ef9-4c1a-999d-b8d1bbb64f4f", PaymentType.SINGLE, null)),
                withPaymentId ? "0000d1d31c8a99bebff069dff194611c" : null,
                AUTHENTICATION_MEANS,
                SIGNER,
                REST_TEMPLATE_MANAGER_MOCK,
                null,
                AUTH_MEANS_REFERENCE);
    }

    private String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
