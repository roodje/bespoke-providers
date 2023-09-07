package com.yolt.providers.openbanking.ais.monzogroup.pis;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoApp;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoTestUtilV2;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This test contains all creation failed flows occurring in Monzo bank provider.
 * <p>
 * Covered flows:
 * - creating payment with invalid currency
 * - creating payment with invalid account scheme
 * - creating UK domestic payment with invalid account scheme
 * - invalid UK domestic payment to be rejected
 * <p>
 */
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/monzogroup/ob_3.1/pis/payment-creation-failed/", httpsPort = 0, port = 0)
@ActiveProfiles("monzogroup")
public class MonzoPaymentProviderV5CreationFailedExceptionIntegrationTest {

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
    public void shouldThrowPaymentExecutionTechnicalExceptionWhenCreateUkDomesticPaymentWithInvalidAccountScheme() {
        // given
        final InitiateUkDomesticPaymentRequestDTO initiateRequest = MonzoTestUtilV2.createValidInitiateRequestForUkDomesticPayment(AccountIdentifierScheme.IBAN);
        InitiateUkDomesticPaymentRequest request = MonzoTestUtilV2.createInitiateRequestDTO(
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                authenticationMeansReference,
                initiateRequest
        );

        // when
        final ThrowingCallable paymentCallable = () -> paymentProvider.initiateSinglePayment(request);

        // then
        assertThatThrownBy(paymentCallable)
                .hasMessage("request_creation_error")
                .isExactlyInstanceOf(PaymentExecutionTechnicalException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("Monzo PIS v3.1 support only SortCodeAccountNumber. Provided scheme: UK.OBIE.IBAN is not supported.");
    }

    @Test
    public void shouldReturnPaymentStatusRejectedForDomesticPaymentWithInvalidAmount() {
        // given
        InitiateUkDomesticPaymentRequest request = MonzoTestUtilV2.createInitiateRequestDTO(
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                authenticationMeansReference,
                createInvalidInitiateRequestForUkDomesticPaymentToBeRejectedDueToNegativeAmount()
        );

        // when
        final InitiateUkDomesticPaymentResponseDTO initiateUkDomesticPaymentResponseDTO = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(initiateUkDomesticPaymentResponseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                });
    }

    @Test
    public void shouldReturnPaymentStatusInitiationErrorWhenDomesticPaymentConsentReturnsHttp400() {
        // given
        InitiateUkDomesticPaymentRequest request = MonzoTestUtilV2.createInitiateRequestDTO(
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                authenticationMeansReference,
                createSampleInitiateRequestForUkDomesticPaymentThatWillCauseHttp400()
        );

        // when
        final InitiateUkDomesticPaymentResponseDTO initiateUkDomesticPaymentResponseDTO = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(initiateUkDomesticPaymentResponseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("OB-ERR-123");
                });
    }

    private InitiateUkDomesticPaymentRequestDTO createInvalidInitiateRequestForUkDomesticPaymentToBeRejectedDueToNegativeAmount() {
        return MonzoTestUtilV2.createSampleInitiateRequestDTO(AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "-100.00");
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestForUkDomesticPaymentThatWillCauseHttp400() {
        return MonzoTestUtilV2.createSampleInitiateRequestDTO(AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "10.00");
    }
}
