package com.yolt.providers.openbanking.ais.aibgroup.pis.ni;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.constants.OAuth.STATE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment happy flows occuring in AIB(NI).
 * <p>
 * Covered flows:
 * - successful return of consent page url
 * - successful creation of payment
 * - successful confirmation of payment
 * - successful getStatus of payment
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AibGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("aib")
@AutoConfigureWireMock(stubs = "classpath:/stubs/aibgroup/ni/pis-3.1.1_single/happy-flow", httpsPort = 0, port = 0)
public class AibNiPaymentProviderHappyFlowIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545";
    private static final String AUTHORIZATION_CODE = "?code=test_code";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final Signer SIGNER = new SignerMock();
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
    public void shouldCreateDomesticPayment(GenericBaseSepaPaymentProvider subject) throws JsonProcessingException {
        // given
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d0a9b85f-9715-4d16-a33d-4323ceab5254");
        var paymentRequest = createPaymentRequest(createInitiateRequestDTO("ACME Inc creditor", null));

        // when
        var response = subject.initiatePayment(paymentRequest);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=someClientId")
                .contains("state=" + STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + STATE)
                .contains("redirect_uri=https%3A%2F%2Fwww.yolt.com%2Fcallback")
                .contains("request=");

        var state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58923", PaymentType.SINGLE, """
                        {"InstructionIdentification":"20201202002028103-4b1t742n-102","EndToEndIdentification":"B7F2761C","LocalInstrument":"UK.OBIE.FPS","InstructedAmount":{"Amount":"10000.00","Currency":"EUR"},"CreditorAccount":{"SchemeName":"UK.OBIE.IBAN","Identification":"GB77BOFI60161331926819","Name":"ACME Inc creditor"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");

        assertThat(response.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Authorised");
                });
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldCreateDomesticPaymentWithDebtor(GenericBaseSepaPaymentProvider subject) throws JsonProcessingException {
        // given
        var paymentRequest = createPaymentRequest(createInitiateRequestDTO("ACME Inc creditor", "ACME Inc debtor"));

        // when
        var response = subject.initiatePayment(paymentRequest);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=someClientId")
                .contains("state=" + STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + STATE)
                .contains("redirect_uri=https%3A%2F%2Fwww.yolt.com%2Fcallback")
                .contains("request=");

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58926", PaymentType.SINGLE, """
                        {"InstructionIdentification":"20201202002028103-4b1t742n-102","EndToEndIdentification":"B7F2761C","InstructedAmount":{"Amount":"10000.00","Currency":"EUR"},"DebtorAccount":{"SchemeName":"UK.OBIE.IBAN","Identification":"GB77BOFI60111111926819","Name":"ACME Inc debtor"},"CreditorAccount":{"SchemeName":"UK.OBIE.IBAN","Identification":"GB77BOFI60161331926819","Name":"ACME Inc creditor"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");

        assertThat(response.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                });
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldSubmitDomesticPayment(GenericBaseSepaPaymentProvider subject) throws JsonProcessingException {
        // given
        String providerState = """
                {"consentId":"58923","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20200515101750462-522347ee-5e0\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\\"LocalInstrument\\":\\"UK.OBIE.FPS\\",\\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"EUR\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.IBAN\\",\\"Identification\\":\\"GB77BOFI60161331926819\\",\\"Name\\":\\"ACME Inc creditor\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Remittance Unstructured\\",\\"Reference\\":\\"REF0123456789-0123\\"}}"}""";
        var request = createConfirmPaymentRequest(providerState);

        // when
        var response = subject.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo("7290-003");

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58923", PaymentType.SINGLE, """
                        {"InstructionIdentification":"20201202002028103-4b1t742n-102","EndToEndIdentification":"B7F2761C","LocalInstrument":"UK.OBIE.FPS","InstructedAmount":{"Amount":"10000.00","Currency":"EUR"},"CreditorAccount":{"SchemeName":"UK.OBIE.IBAN","Identification":"GB77BOFI60161331926819","Name":"ACME Inc creditor"},"RemittanceInformation":{"Unstructured":"Remittance Unstructured","Reference":"REF0123456789-0123"}}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementInProcess");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldGetConsentStatusDomesticPayment(GenericBaseSepaPaymentProvider subject) throws JsonProcessingException {
        // given
        var request = createGetStatusRequest("58920", null);

        // when
        var response = subject.getStatus(request);

        // then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58920", PaymentType.SINGLE, """
                        {"Status":"Authorised","resourceId":"58920"}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Authorised");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));

    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldGetStatusDomesticPayment(GenericBaseSepaPaymentProvider subject) throws JsonProcessingException {
        // given
        var request = createGetStatusRequest("58924", "7290-004");

        // when
        var response = subject.getStatus(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo("7290-004");

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("58924", PaymentType.SINGLE, """
                        {"Status":"AcceptedSettlementCompleted","resourceId":"7290-04"}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementCompleted");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    private SepaInitiatePaymentRequestDTO createInitiateRequestDTO(String creditorName, String debtorName) {
        DynamicFields dynamicFields = new DynamicFields();
        SepaAccountDTO debtorAccount = null;
        if (StringUtils.hasText(debtorName)) {
            dynamicFields.setDebtorName(debtorName);
            debtorAccount = new SepaAccountDTO(CurrencyCode.EUR, "GB77BOFI60111111926819");
        }

        dynamicFields.setRemittanceInformationStructured("REF0123456789-0123");

        SepaAccountDTO creditorAccount = new SepaAccountDTO(CurrencyCode.EUR, "GB77BOFI60161331926819");

        return SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(creditorAccount)
                .debtorAccount(debtorAccount)
                .endToEndIdentification("B7F2761C")
                .creditorName(creditorName)
                .instructedAmount(new SepaAmountDTO(new BigDecimal("10000.00")))
                .remittanceInformationUnstructured("Remittance Unstructured")
                .dynamicFields(dynamicFields)
                .build();
    }


    private GetStatusRequest createGetStatusRequest(String consentId, String paymentId) {
        return new GetStatusRequest(createUkProviderState(new UkProviderState(consentId, PaymentType.SINGLE, "")),
                paymentId != null ? paymentId : null,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                null,
                authenticationMeansReference);
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

    private String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
