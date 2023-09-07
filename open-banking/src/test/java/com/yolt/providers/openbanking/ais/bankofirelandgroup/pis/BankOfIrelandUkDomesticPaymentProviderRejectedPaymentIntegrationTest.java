package com.yolt.providers.openbanking.ais.bankofirelandgroup.pis;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
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
 * This test contains creation and confirmation of uk payment when payment status is rejected occurring in HSBC.
 * <p>
 * Covered flows:
 * - rejected creation of payment
 * - rejected confirmation of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BankOfIrelandGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bankofireland")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/bankofireland/pis-3.0.0/single/rejected-payment",
        "classpath:/stubs/bankofireland/pis-3.0.0/single/happy-flow/grant-type"}, httpsPort = 0, port = 0)
class BankOfIrelandUkDomesticPaymentProviderRejectedPaymentIntegrationTest {

    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final String INVALID_CREDITOR_NUMBER = "1802968485593088";
    private static final String INVALID_DEBTOR_NUMBER = "8272908780568576";
    private final AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @Autowired
    @Qualifier("BankOfIrelandPaymentProviderV1")
    private GenericBasePaymentProviderV3 bankOfIrelandPaymentProviderV1;

    private Stream<UkDomesticPaymentProvider> getProviders() {
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
    void shouldReturnResultWithStatusInitiationErrorForCreatePaymentWhenStatusRejectedFromDomesticPaymentConsentsEndpoint(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
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
        InitiateUkDomesticPaymentResponseDTO result = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnResultWithStatusUnknownForConfirmPaymentWhenStatusRejectedFromDomesticPaymentsEndpoint(PaymentSubmissionProvider paymentProvider) throws ConfirmationFailedException {
        // given
        String providerState = """
                {"consentId":"bbbbc664f984571b5a20ea666a7d0c1","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"FEF32557\\",\\"InstructedAmount\\":{\\"Amount\\":\\"100.00\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"8272908780568576\\",\\"Name\\":\\"Alex Mitchell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"RemittanceInformation\\":{\\"Reference\\":\\"PaymentToReject\\"}}"}""";
        SubmitPaymentRequest request = createUkDomesticConfirmPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO result = paymentProvider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                });
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO() {
        return new InitiateUkDomesticPaymentRequestDTO(
                "FEF32557",
                CurrencyCode.GBP.toString(),
                new BigDecimal("100.00"),
                new UkAccountDTO(INVALID_CREDITOR_NUMBER, SORTCODEACCOUNTNUMBER, "Jordan Bell", null),
                new UkAccountDTO(INVALID_DEBTOR_NUMBER, SORTCODEACCOUNTNUMBER, "Alex Mitchell", null),
                null,
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "PaymentToReject"));
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
                authenticationMeansReference);
    }


}