package com.yolt.providers.openbanking.ais.rbsgroup.pis.v10;

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
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
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
 * This test contains all uk payment happy flows occurring in RBS.
 * <p>
 * Covered flows:
 * - successful return of consent page url
 * - successful creation of payment
 * - successful confirmation of payment
 * - successful getting status of payment
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {RbsApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/pis", httpsPort = 0, port = 0)
class RbsGroupUkDomesticPaymentProviderIntegrationTest {

    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STUBBED_PAYMENT_SUBMISSION_ID = "e23f5d5cd08d44c3993243ad3f19d56e";

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final String VALID_CREDITOR_NUMBER = "12345678901234";
    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("NatWestPaymentProviderV11")
    private GenericBasePaymentProviderV2 natWestPaymentProviderV11;

    @Autowired
    @Qualifier("RoyalBankOfScotlandPaymentProviderV11")
    private GenericBasePaymentProviderV2 royalBankOfScotlandPaymentProviderV11;

    @Autowired
    @Qualifier("UlsterBankPaymentProviderV10")
    private GenericBasePaymentProviderV2 ulsterBankPaymentProviderV10;

    private Stream<UkDomesticPaymentProvider> getPecAwareProviders() {
        return Stream.of(natWestPaymentProviderV11,
                royalBankOfScotlandPaymentProviderV11,
                ulsterBankPaymentProviderV10);
    }

    private Stream<PaymentSubmissionProvider> getPecAwareSubmissionProviders() {
        return Stream.of(natWestPaymentProviderV11,
                royalBankOfScotlandPaymentProviderV11,
                ulsterBankPaymentProviderV10);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForPis();
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getPecAwareProviders")
    public void shouldReturnResponseWithAuthorizationUrlAndPecMetadataWithInitiationSuccessStatusForInitiateSinglePaymentWhenCorrectDataAreProvided(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO(VALID_CREDITOR_NUMBER);
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
                .contains("client_id=someClientId")
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
    @MethodSource("getPecAwareSubmissionProviders")
    public void shouldReturnResponseWithPaymentIdAndPecMetadataWithAcceptedStatusForSubmitPaymentWhenCorrectDataAreProvided(PaymentSubmissionProvider paymentProvider) throws GeneralConfirmException {
        // given
        String providerState = """
                {"consentId":"bec2bc664f984571b5a20ea666a7d0c1","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\":\\"2513bfeg\\",\\"EndToEndIdentification\\":\\"35B64F93\\",\\"InstructedAmount\\":{\\"Amount\\":\\"0.01\\",\\"Currency\\":\\"GBP\\"},\\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"12345678901234\\",\\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\":{\\"Unstructured\\":\\"SomeRandomMessage\\",\\"Reference\\":\\"SomeRandomMessage2\\"}}"}""";
        SubmitPaymentRequest request = createUkDomesticConfirmPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentId()).isEqualTo(STUBBED_PAYMENT_SUBMISSION_ID);
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementInProcess");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPecAwareSubmissionProviders")
    public void shouldReturnResponseWithPaymentIdAndPecMetadataWithCompletedStatusForGetStatusWhenPaymentIdIsProvidedInRequest(PaymentSubmissionProvider paymentProvider) {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(true);

        // when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentId()).isEqualTo("e23f5d5cd08d44c3993243ad3f19d56e");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedCreditSettlementCompleted");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPecAwareSubmissionProviders")
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

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO(String identification) {
        UkAccountDTO creditorAccount = new UkAccountDTO(identification, AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "P. Jantje", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "35B64F93",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                "SomeRandomMessage",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "SomeRandomMessage2")
        );
    }

    private SubmitPaymentRequest createUkDomesticConfirmPaymentRequest(String providerState) {
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
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

    private GetStatusRequest creteUkDomesticGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequest(withPaymentId ? null : createUkProviderState(new UkProviderState("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, null)),
                withPaymentId ? "e23f5d5cd08d44c3993243ad3f19d56e" : null,
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
}
