package com.yolt.providers.openbanking.ais.santander.pis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.santander.SantanderApp;
import com.yolt.providers.openbanking.ais.santander.SantanderSampleAuthenticationMeansV2;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.common.Constants.CLIENT_ID_YOLT;
import static com.yolt.providers.common.Constants.CLIENT_REDIRECT_URL_ID_YOLT_APP;
import static com.yolt.providers.openbanking.ais.santander.auth.SantanderAuthMeansMapper.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment happy flows occuring in Santander.
 * <p>
 * Disclaimer: Santander is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - successful creation of uk domestic payment including consent page url return
 * - successful submission of uk domestic payment
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SantanderApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/santander/pis-3.1.19/happy-flow"}, httpsPort = 0, port = 0)
@ActiveProfiles("santander")
class SantanderUkDomesticPaymentProviderHappyFlowIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback?code=";
    private static final String STUBBED_PAYMENT_SUBMISSION_ID = "10168215535616";
    private static final Signer SIGNER = new SignerMock();

    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
    private static final String TEST_PSU_IP_ADDRESS = "12.34.56.78";
    private static final String TEST_STATE = "6d1d0bd4-2536-467e-9bdf-a69ef6c21a65";

    private RestTemplateManagerMock restTemplateManagerMock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("SantanderPaymentProviderV15")
    private GenericBasePaymentProviderV2 santanderPaymentProviderV15;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "1626df30-50ad-42d8-8f39-40dd95f4b15f");
        authenticationMeans = new SantanderSampleAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldCreateUkDomesticPayment(GenericBasePaymentProviderV2 provider) {
        InitiateUkDomesticPaymentRequestDTO requestDTO = createSampleInitiateRequestDTO();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = provider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=422a2265-a061-4007-99b3-ceeb64e85077")
                .contains("state=" + TEST_STATE)
                .contains("scope=openid+payments")
                .contains("nonce=" + TEST_STATE)
                .contains("redirect_uri=https%3A%2F%2Fwww.yolt.com%2Fcallback")
                .contains("request=");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldSubmitUkDomesticPayment(GenericBasePaymentProviderV2 provider) {
        // given
        String providerState = """
                {"consentId":"4851833560367104","paymentType":"SINGLE",\
                "openBankingPayment":"{\\"InstructionIdentification\\":\\"1938883646324736\\",\
                \\"EndToEndIdentification\\":\\"2707380502528000\\",\
                \\"InstructedAmount\\":{\\"Amount\\":\\"100.0\\",\
                \\"Currency\\":\\"JPY\\"},\
                \\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\
                \\"Identification\\":\\"1802968485593088\\",\
                \\"Name\\":\\"Jordan Bell\\"},\
                \\"RemittanceInformation\\":{\\"Unstructured\\":\\"onmobhetelalobucmogigelecarreocukaezeji\\"}}"}\
                """;
        SubmitPaymentRequest request = createConfirmPaymentRequest(providerState);

        // when
        PaymentStatusResponseDTO response = provider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEqualTo(STUBBED_PAYMENT_SUBMISSION_ID);
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Pending");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnResponseWithPaymentIdAndPecMetadataWithCompletedStatusForGetStatusWhenPaymentIdIsProvidedInRequest(GenericBasePaymentProviderV2 provider) {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(true);

        // when
        PaymentStatusResponseDTO response = provider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEqualTo("10168215535616");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedCreditSettlementCompleted");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnResponseWithConsentIdAndPecMetadataWithInitiationSuccessStatusForGetStatusWhenPaymentIdIsNotProvidedInRequest(GenericBasePaymentProviderV2 provider) throws JsonProcessingException {
        // given
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest(false);

        // when
        PaymentStatusResponseDTO response = provider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("GENERIC-P-PAYMENT_ID", PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"GENERIC-P-PAYMENT_ID"}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnTransportKeyRequirements(GenericBasePaymentProviderV2 provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnSigningKeyRequirements(GenericBasePaymentProviderV2 provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnTypedAuthenticationMeans(GenericBasePaymentProviderV2 provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans)
                .hasSize(8)
                .containsOnlyKeys(
                        INSTITUTION_ID_NAME,
                        CLIENT_ID_NAME,
                        PRIVATE_SIGNING_KEY_HEADER_ID_NAME,
                        SIGNING_PRIVATE_KEY_ID_NAME,
                        TRANSPORT_CERTIFICATE_NAME,
                        TRANSPORT_PRIVATE_KEY_ID_NAME,
                        ORGANIZATION_ID_NAME,
                        SOFTWARE_ID_NAME
                );
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO() {
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "2707380502528000",
                CurrencyCode.JPY.toString(),
                new BigDecimal("100.0"),
                creditorAccount,
                null,
                "onmobhetelalobucmogigelecarreocukaezeji",
                new HashMap<>()
        );
    }

    private SubmitPaymentRequest createConfirmPaymentRequest(String providerState) {
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payments#code=" + authorizationCode,
                SIGNER,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private GetStatusRequest creteUkDomesticGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequest(withPaymentId ? null : createUkProviderState(new UkProviderState("GENERIC-P-PAYMENT_ID", PaymentType.SINGLE, null)),
                withPaymentId ? "10168215535616" : null,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference);
    }

    private static String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Stream<GenericBasePaymentProviderV2> getPaymentProviders() {
        return Stream.of(santanderPaymentProviderV15);
    }
}