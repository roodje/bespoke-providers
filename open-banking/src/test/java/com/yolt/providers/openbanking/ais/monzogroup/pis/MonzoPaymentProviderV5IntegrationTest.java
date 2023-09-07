package com.yolt.providers.openbanking.ais.monzogroup.pis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoApp;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoTestUtilV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.MonzoGroupBasePaymentProviderV2;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy payment flows occurring in Monzo bank provider.
 * <p>
 * Covered flows:
 * - creating payment
 * - returning payment consent page URL
 * - submitting payment
 * - creating UK domestic payment with sort code account number
 * - submitting UK domesting payment
 * - deleting consent on bank side
 * <p>
 */
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/monzogroup/ob_3.1/pis/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("monzogroup")
public class MonzoPaymentProviderV5IntegrationTest {

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");

    private static final String TEST_STATE = "aTestState";
    private static final String STUBBED_PAYMENT_SUBMISSION_ID = "obdompayment_00009myntSAhYTWLCFJqbZ";
    private RestTemplateManagerMock restTemplateManagerMock;
    private Signer signer = new SignerMock();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("MonzoPaymentProviderV5")
    private MonzoGroupBasePaymentProviderV2 paymentProvider;

    private AuthenticationMeansReference authenticationMeansReference;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new MonzoSampleTypedAuthMeansV2().getAuthenticationMeans();
        authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "54321");
    }

    @Test
    public void shouldCreateUkDomesticPaymentWithSortCodeAccountNumber() {
        // given
        final InitiateUkDomesticPaymentRequestDTO initiateRequest = MonzoTestUtilV2.createValidInitiateRequestForUkDomesticPayment(AccountIdentifierScheme.SORTCODEACCOUNTNUMBER);
        InitiateUkDomesticPaymentRequest request = MonzoTestUtilV2.createInitiateRequestDTO(
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                authenticationMeansReference,
                initiateRequest
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=someClientId")
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fwww.yolt.com%2Fcallback-test")
                .contains("request=");

        assertThat(response.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                });
    }

    @Test
    public void shouldSubmitUkDomesticPayment() {
        // given
        String providerState = """
                {"consentId":"obpispdomesticpaymentconsent_00009mynmtLy5yvfOeJjqD","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20210202075444419-52254e2c-0d8\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"LocalInstrument\\":\\"UK.OBIE.FPS\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"8272908780568576\\",\\"Name\\":\\"Alex Mitchell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\",\\"Reference\\":\\"Structured\\"}}"}""";
        SubmitPaymentRequest request = MonzoTestUtilV2.createConfirmPaymentRequestGivenProviderState(authenticationMeans, signer, restTemplateManagerMock, authenticationMeansReference, providerState);

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo(STUBBED_PAYMENT_SUBMISSION_ID);
        assertThat(response.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Pending");
                });
    }

    @Test
    public void shouldReturnResponseWithPaymentIdAndPecMetadataWithCompletedStatusForGetStatusWhenPaymentIdIsProvidedInRequest() {
        //when
        GetStatusRequest getStatusRequest = MonzoTestUtilV2.createGetStatusRequest(authenticationMeans, signer, restTemplateManagerMock, authenticationMeansReference, true);

        //then
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEqualTo("e23f5d5cd08d44c3993243ad3f19d56e");
        assertThat(response.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedCreditSettlementCompleted");
                });
    }

    @Test
    public void shouldReturnResponseWithConsentIdAndPecMetadataWithInitiationSuccessStatusForGetStatusWhenPaymentIdIsNotProvidedInRequest() throws JsonProcessingException {
        //when
        GetStatusRequest getStatusRequest = MonzoTestUtilV2.createGetStatusRequest(authenticationMeans, signer, restTemplateManagerMock, authenticationMeansReference, false);

        //then
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"331d76df48ed41229b67f062dd55e340"}""");

        assertThat(response.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                });
    }
}