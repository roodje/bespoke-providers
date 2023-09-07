package com.yolt.providers.openbanking.ais.hsbcgroup.pis.single;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
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

import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment flows ending with payment rejection in HSBC.
 * <p>
 * Disclaimer: as all providers in HSBC group are the same from code and stubs perspective (the only difference is configuration)
 * we are running parametrized tests for testing, so we'll cover all payment providers from HSBC group
 * <p>
 * Covered flows:
 * - when payment creation ends with rejection status we throw CreationFailedException
 * - when payment confirmation ends with rejection status we throw ConfirmationFailedException
 * - when authorization fails with Access Denied we throw PaymentCancelledException
 * - when authorization fails with server error in url we throw with ConfirmationFailedException
 * - when submission of payment fails due to invalid request we throw ConfirmationFailedException
 * - when UK domestic payment creation fails due to invalid request with rejection status we throw CreationFailedException
 * - when submission of UK domestic payment fails due to invalid request we throw ConfirmationFailedException
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/hsbcgroup/pis-3.1.6/single/rejected-payment",
        "classpath:/stubs/hsbcgroup/pis-3.1.6/single/happy-flow"}, httpsPort = 0, port = 0)
class HsbcGroupPaymentProviderRejectedPaymentsIntegrationTest {

    private static final String AUTHORIZATION_CODE = "iVuATHQHLIjtGjeuxtBj6Gfnd8o";
    private static final String EXTERNAL_CONSENT_ID = "bbbbc664f984571b5a20ea666a7d0c1";
    private static final String TEST_REDIRECT_URL = "https://www.yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String END_TO_END_IDENTIFICATION = "FEF32557";
    private static final String INSTRUCTION_IDENTIFICATION = "20201203108401220-50afa69f-ad6";
    private static final SignerMock SIGNER = new SignerMock();
    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-100.00");
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("0.01");

    private final HsbcGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new HsbcGroupSampleAuthenticationMeansV2();
    private final AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("HsbcPaymentProviderV13")
    private GenericBasePaymentProviderV2 hsbcPaymentProviderV13;

    @Autowired
    @Qualifier("HsbcPaymentProviderV14")
    private GenericBasePaymentProviderV3 hsbcPaymentProviderV14;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV13")
    private GenericBasePaymentProviderV2 firstDirectPaymentProviderV13;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV14")
    private GenericBasePaymentProviderV3 firstDirectPaymentProviderV14;


    private Stream<UkDomesticPaymentProvider> getUkDomesticPaymentProvider() {
        return Stream.of(
                hsbcPaymentProviderV13, firstDirectPaymentProviderV13,
                hsbcPaymentProviderV14, firstDirectPaymentProviderV14);
    }

    private Stream<PaymentSubmissionProvider> getPaymentSubmissionProvider() {
        return Stream.of(
                hsbcPaymentProviderV13, firstDirectPaymentProviderV13,
                hsbcPaymentProviderV14, firstDirectPaymentProviderV14);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d10f24f4-032a-4843-bfc9-22b599c7ae2d");
        authenticationMeans = sampleAuthenticationMeans.getHsbcGroupSampleAuthenticationMeansForPis();
    }


    @ParameterizedTest
    @MethodSource("getUkDomesticPaymentProvider")
    public void shouldReturnInitiationErrorStatusWhenInvalidUkDomesticPaymentToBeRejected(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequest request = createUkDomesticPaymentRequest(createInitiateUkDomesticPaymentWithNegativeAmount());

        // when
        InitiateUkDomesticPaymentResponseDTO result = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @ParameterizedTest
    @MethodSource("getUkDomesticPaymentProvider")
    public void shouldThrowPaymentExecutionTechnicalExceptionWhenRemittanceInformationHasIllegalCharacters(UkDomesticPaymentProvider paymentProvider) {
        // given
        InitiateUkDomesticPaymentRequest request = createUkDomesticPaymentRequest(createInitiateUkDomesticPaymentWithIllegalCharactersInRemittance());

        // when
        ThrowingCallable paymentCallable = () -> paymentProvider.initiateSinglePayment(request);

        // then
        AssertionsForClassTypes
                .assertThatThrownBy(paymentCallable)
                .isExactlyInstanceOf(PaymentExecutionTechnicalException.class)
                .hasMessageContaining("request_creation_error");
    }

    @ParameterizedTest
    @MethodSource("getPaymentSubmissionProvider")
    public void shouldReturnUnknownStatusWhenSubmitInvalidUkDomesticPayment(PaymentSubmissionProvider paymentProvider) throws ConfirmationFailedException {
        // given
        String providerStateWithNegativeAmount = "{\"consentId\":\"bbbbc664f984571b5a20ea666a7d0c1\",\"paymentType\":\"SINGLE\",\"openBankingPayment\":\"{\\\"InstructionIdentification\\\":\\\"20201203108401220-50afa69f-ad6\\\",\\\"EndToEndIdentification\\\":\\\"FEF32557\\\",\\\"InstructedAmount\\\":{\\\"Amount\\\":\\\"-100.00\\\",\\\"Currency\\\":\\\"GBP\\\"},\\\"DebtorAccount\\\":{\\\"SchemeName\\\":\\\"UK.OBIE.SortCodeAccountNumber\\\",\\\"Identification\\\":\\\"8272908780568576\\\",\\\"Name\\\":\\\"Alex Mitchell\\\"},\\\"CreditorAccount\\\":{\\\"SchemeName\\\":\\\"UK.OBIE.SortCodeAccountNumber\\\",\\\"Identification\\\":\\\"1802968485593088\\\",\\\"Name\\\":\\\"Jordan Bell\\\"},\\\"RemittanceInformation\\\":{\\\"Reference\\\":\\\"PaymentToReject\\\"}}\"}";
        SubmitPaymentRequest request = createConfirmPaymentRequestGivenProviderState(providerStateWithNegativeAmount);

        // when
        PaymentStatusResponseDTO result = paymentProvider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

    private SubmitPaymentRequest createConfirmPaymentRequestGivenProviderState(String providerState) {
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                "https://www.yolt.com/callback-test/payments/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0?code=" + AUTHORIZATION_CODE,
                SIGNER,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private InitiateUkDomesticPaymentRequest createUkDomesticPaymentRequest(InitiateUkDomesticPaymentRequestDTO requestDTO) {
        return new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private InitiateUkDomesticPaymentRequestDTO createInitiateUkDomesticPaymentWithNegativeAmount() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                END_TO_END_IDENTIFICATION,
                CurrencyCode.GBP.toString(),
                NEGATIVE_AMOUNT,
                creditorAccount,
                debtorAccount,
                null,
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "PaymentToReject")
        );
    }

    private InitiateUkDomesticPaymentRequestDTO createInitiateUkDomesticPaymentWithIllegalCharactersInRemittance() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                END_TO_END_IDENTIFICATION,
                CurrencyCode.GBP.toString(),
                VALID_AMOUNT,
                creditorAccount,
                debtorAccount,
                null,
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "Pay'ment")
        );
    }

    private ProviderAccountNumberDTO createDebtorAccountNumberDTO() {
        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(SORTCODEACCOUNTNUMBER, "8272908780568576");
        providerAccountNumberDTO.setHolderName("Alex Mitchell");

        return providerAccountNumberDTO;
    }

    private ProviderAccountNumberDTO createCreditorAccountNumberDTO() {
        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(SORTCODEACCOUNTNUMBER, "1802968485593088");
        providerAccountNumberDTO.setHolderName("Jordan Bell");

        return providerAccountNumberDTO;
    }
}