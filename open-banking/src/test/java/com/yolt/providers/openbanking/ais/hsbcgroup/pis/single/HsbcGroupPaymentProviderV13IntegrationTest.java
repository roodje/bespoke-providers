package com.yolt.providers.openbanking.ais.hsbcgroup.pis.single;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
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

import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment happy flows occuring in HSBC.
 * <p>
 * Disclaimer: as all providers in HSBC group are the same from code and stubs perspective (the only difference is configuration)
 * we are running parametrized tests for testing, so we'll cover all payment providers from HSBC group
 * <p>
 * Covered flows:
 * - successful return of consent page url
 * - successful creation of payment
 * - successful creation of UK domestic payment with IBAN
 * - successful creation of UK domestic payment with sortcode account number
 * - successful submission of payment
 * - successful submission of UK domestic payment
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/hsbcgroup/pis-3.1.6/single"}, httpsPort = 0, port = 0)
@Deprecated
class HsbcGroupPaymentProviderV13IntegrationTest {

    private static final String AUTHORIZATION_CODE = "iVuATHQHLIjtGjeuxtBj6Gfnd8o";
    private static final String TEST_REDIRECT_URL = "https://www.yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STUBBED_PAYMENT_SUBMISSION_ID = "0000f7f237ee4e6eaff0c3df18246676";
    private static final String END_TO_END_IDENTIFICATION = "35B64F93";
    private static final String REFERENCE = "REF0123456789-0123";
    private static final String REMITTANCE_UNSTRUCTURED = "Remittance Unstructured";
    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("0.01");

    private static final SignerMock SIGNER = new SignerMock();

    private final HsbcGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new HsbcGroupSampleAuthenticationMeansV2();
    private final AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("HsbcPaymentProviderV13")
    private GenericBasePaymentProviderV2 hsbcPaymentProviderV13;

    @Autowired
    @Qualifier("FirstDirectPaymentProviderV13")
    private GenericBasePaymentProviderV2 firstDirectPaymentProviderV13;

    private Stream<GenericBasePaymentProviderV2> getPaymentDataProviders() {
        return Stream.of(
                hsbcPaymentProviderV13,
                firstDirectPaymentProviderV13);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d10f24f4-032a-4843-bfc9-22b599c7ae2d");
        authenticationMeans = sampleAuthenticationMeans.getHsbcGroupSampleAuthenticationMeansForPis();
    }

    @ParameterizedTest
    @MethodSource("getPaymentDataProviders")
    public void shouldCreateUkDomesticPaymentWithIban(GenericBasePaymentProviderV2 paymentProvider) {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createUkDomesticPaymentWithIban();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                SIGNER,
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
                .contains("redirect_uri=https%3A%2F%2Fwww.yolt.com%2Fcallback-test")
                .contains("request=");
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentDataProviders")
    public void shouldCreateUkDomesticPaymentWithSortCodeAccountNumber(GenericBasePaymentProviderV2 paymentProvider) {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = createUkDomesticPaymentWithSortCodeAccountNumber();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                SIGNER,
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
                .contains("redirect_uri=https%3A%2F%2Fwww.yolt.com%2Fcallback-test")
                .contains("request=");
    }

    @ParameterizedTest
    @MethodSource("getPaymentDataProviders")
    public void shouldSubmitUkDomesticPayment(GenericBasePaymentProviderV2 paymentProvider) {
        // given
        String providerState = """
                {"consentId":"462d1a04-4g74-42d7-ba69-0bbe1e5fef68","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"20201203108401220-50afa69f-ad6\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"8272908780568576\\",\\"Name\\":\\"Alex Mitchell\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"1802968485593088\\",\\"Name\\":\\"Jordan Bell\\"},\\"RemittanceInformation\\":{\\"Reference\\":\\"REF0123456789-0123\\", \\"Unstructured\\":\\"Remittance Unstructured\\"}}"}""";
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

    private InitiateUkDomesticPaymentRequestDTO createUkDomesticPaymentWithSortCodeAccountNumber() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                END_TO_END_IDENTIFICATION,
                CurrencyCode.GBP.toString(),
                VALID_AMOUNT,
                creditorAccount,
                debtorAccount,
                REMITTANCE_UNSTRUCTURED,
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, REFERENCE)
        );
    }

    private InitiateUkDomesticPaymentRequestDTO createUkDomesticPaymentWithIban() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.IBAN, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.IBAN, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                END_TO_END_IDENTIFICATION,
                CurrencyCode.GBP.toString(),
                VALID_AMOUNT,
                creditorAccount,
                debtorAccount,
                REMITTANCE_UNSTRUCTURED,
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, REFERENCE)
        );
    }

    private SubmitPaymentRequest createUkDomesticConfirmPaymentRequest(String providerState) {
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                "https://www.yolt.com/callback-test/payments/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0#code=" + AUTHORIZATION_CODE,
                SIGNER,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }
}