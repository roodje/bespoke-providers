package com.yolt.providers.openbanking.ais.santander.pis;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains creation of uk payment when bad request HTTP status code is returned from bank
 * <p>
 * Disclaimer: Santander is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - HTTP-400 Bad Request upon creation of payment
 * - HTTP-400 Bad Request upon submission of payment
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SantanderApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/santander/pis-3.1.19/http-400/", httpsPort = 0, port = 0)
@ActiveProfiles("santander")
class SantanderUkDomesticPaymentProviderHttp400IntegrationTest {

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final String TEST_REDIRECT_URL = "https://yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("SantanderPaymentProviderV15")
    private GenericBasePaymentProviderV2 santanderPaymentProviderV15;

    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private Signer signer = new SignerMock();

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new SantanderSampleAuthenticationMeansV2().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "1626df30-50ad-42d8-8f39-40dd95f4b15f");
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnResultWithStatusInitiationErrorInPecMetadataForCreatePaymentWhenBadRequestHttpCodeIsReceivedFromBank(GenericBasePaymentProviderV2 provider) {
        // given
        InitiateUkDomesticPaymentRequest request = createInvalidInitiatePaymentRequest();

        // when
        InitiateUkDomesticPaymentResponseDTO result = provider.initiateSinglePayment(request);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("OB-ERR-123");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            [{"ErrorCode":"OB-ERR-123","Message":"Invalid Payment Request Body","Path":"santander/v3.1/pisp/domestic-payment-consents","Url":"santander/v3.1/pisp/domestic-payment-consents"}]""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldReturnResultWithStatusExecutionFailedForConfirmPaymentWhenBadRequestHttpCodeIsReceivedFromBank(GenericBasePaymentProviderV2 provider) {
        // given
        SubmitPaymentRequest request = createInvalidSubmitUkPaymentRequestToBeRejected();

        // when
        PaymentStatusResponseDTO result = provider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("OB-ERR-123");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            [{"ErrorCode":"OB-ERR-123","Message":"Invalid Confirmation Request Body","Path":"santander/v3.1/pisp/domestic-payments","Url":"santander/v3.1/pisp/domestic-payments"}]""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.EXECUTION_FAILED);
                }));
    }

    private InitiateUkDomesticPaymentRequest createInvalidInitiatePaymentRequest() {
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
        InitiateUkDomesticPaymentRequestDTO requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                "2707380502528000",
                CurrencyCode.JPY.toString(),
                new BigDecimal("-100.0"),
                creditorAccount,
                null,
                "onmobhetelalobucmogigelecarreocukaezeji",
                new HashMap<>()
        );
        return new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private SubmitPaymentRequest createInvalidSubmitUkPaymentRequestToBeRejected() {
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        String redirectUrl = "https://www.yolt.com/callback/payments/?code=" + authorizationCode;
        String providerStateWithInvalidPayment = """
                {"consentId":"4851833560367104",\
                "paymentType":"SINGLE",\
                "openBankingPayment":"{\\"InstructionIdentification\\":\\"1938883646324736\\",\
                \\"EndToEndIdentification\\":\\"2707380502528000\\",\
                \\"InstructedAmount\\":{\\"Amount\\":\\"-100.0\\",\
                \\"Currency\\":\\"JPY\\"},\
                \\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\"\
                ,\\"Identification\\":\\"1802968485593088\\",\
                \\"Name\\":\\"Jordan Bell\\"},\
                \\"RemittanceInformation\\":{\\"Unstructured\\":\\"onmobhetelalobucmogigelecarreocukaezeji\\"}}"}""";
        return createUkSubmitPaymentRequest(providerStateWithInvalidPayment, redirectUrl);
    }

    private SubmitPaymentRequest createUkSubmitPaymentRequest(String providerState, String redirectUrl) {
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                redirectUrl,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private Stream<GenericBasePaymentProviderV2> getPaymentProviders() {
        return Stream.of(santanderPaymentProviderV15);
    }
}