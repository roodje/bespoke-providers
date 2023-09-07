package com.yolt.providers.openbanking.ais.monzogroup.pis;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoApp;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoTestUtilV2;
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
 * This test contains all confirmation failed flows occurring in Monzo bank provider.
 * <p>
 * Covered flows:
 * - submiting invalid payment
 * - submiting invalid domestic payment
 * <p>
 */
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/monzogroup/ob_3.1/pis/payment-submission-failed/", httpsPort = 0, port = 0)
@ActiveProfiles("monzogroup")
public class MonzoPaymentProviderV5ConfirmationFailedExceptionTest {

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");

    private RestTemplateManagerMock restTemplateManagerMock;
    private Signer signer = new SignerMock();

    @Autowired
    @Qualifier("MonzoPaymentProviderV5")
    private GenericBasePaymentProviderV2 paymentProvider;

    private AuthenticationMeansReference authenticationMeansReference;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new MonzoSampleTypedAuthMeansV2().getAuthenticationMeans();
        authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "54321");
    }

    @Test
    public void shouldReturnPaymentStatusRejectedForSubmissionOfInvalidUkDomesticPayment() {
        // given
        String providerStateWithInvalidAmount = """
                {"consentId":"obpispdomesticpaymentconsent_00009mynmtLy5yvfOeJjqD","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20210202075444419-52254e2c-0d8\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"LocalInstrument\\":\\"UK.OBIE.FPS\\",\\"InstructedAmount\\":{\\"Amount\\":\\"-100.00\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"8272908780568576\\",\\"Name\\":\\"Alex Mitchell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"RemittanceInformation\\":{\\"Reference\\":\\"Unstructured\\"}}"}""";
        SubmitPaymentRequest request = MonzoTestUtilV2.createConfirmPaymentRequestGivenProviderState(
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                authenticationMeansReference,
                providerStateWithInvalidAmount
        );

        // when
        final PaymentStatusResponseDTO paymentStatusResponseDTO = paymentProvider.submitPayment(request);

        // then
        assertThat(paymentStatusResponseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                });
    }
}
