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
import java.util.stream.Stream;

import static com.yolt.providers.common.Constants.CLIENT_ID_YOLT;
import static com.yolt.providers.common.Constants.CLIENT_REDIRECT_URL_ID_YOLT_APP;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment flows ending with payment rejection in Santander.
 * <p>
 * Disclaimer: Santander is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows for both types of payments:
 * - when creating payment ends with rejection status we throw CreationFailedException
 * - when confirming payment ends with rejection status we throw ConfirmationFailedException
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SantanderApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/santander/pis-3.1.19/rejected-payment"}, httpsPort = 0, port = 0)
@ActiveProfiles("santander")
class SantanderUkDomesticPaymentProviderRejectedPaymentIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback/1ed6cc15-60fe-4b00-adf8-bd8e04e7804e?code=";
    private static final Signer SIGNER = new SignerMock();

    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
    private static final String TEST_PSU_IP_ADDRESS = "12.34.56.78";
    private static final String TEST_STATE = "6d1d0bd4-2536-467e-9bdf-a69ef6c21a65";

    private RestTemplateManagerMock restTemplateManagerMock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("SantanderPaymentProviderV15")
    private GenericBasePaymentProviderV2 santanderPaymentProviderV15;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "a726ed32-9893-4625-b429-b712f1460834");
        authenticationMeans = new SantanderSampleAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldThrowCreationFailedExceptionWhenInvalidUkPaymentToBeRejected(GenericBasePaymentProviderV2 provider) {
        // given
        InitiateUkDomesticPaymentRequest initiateUkDomesticPaymentRequest = createInvalidInitiatePaymentRequestToBeRejected();

        // when
        InitiateUkDomesticPaymentResponseDTO result = provider.initiateSinglePayment(initiateUkDomesticPaymentRequest);

        // then
        assertThat(result.getProviderState()).isNotEmpty();
        assertThat(result.getLoginUrl()).isNotEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldThrowConfirmationFailedExceptionWhenSubmitInvalidUkPaymentToBeRejected(GenericBasePaymentProviderV2 provider) {
        // given
        SubmitPaymentRequest request = createInvalidSubmitUkPaymentRequestToBeRejected();

        // when
        PaymentStatusResponseDTO result = provider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEqualTo("10168215535616");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    private InitiateUkDomesticPaymentRequest createInvalidInitiatePaymentRequestToBeRejected() {
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
                REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                SIGNER,
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
                \\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\
                \\"Identification\\":\\"1802968485593088\\",\
                \\"Name\\":\\"Jordan Bell\\"},\
                \\"RemittanceInformation\\":{\\"Unstructured\\":\\"onmobhetelalobucmogigelecarreocukaezeji\\"}}"}""";
        return createUkSubmitPaymentRequest(providerStateWithInvalidPayment, redirectUrl);
    }

    private SubmitPaymentRequest createUkSubmitPaymentRequest(String providerState, String redirectUrl) {
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                redirectUrl,
                SIGNER,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    private Stream<GenericBasePaymentProviderV2> getPaymentProviders() {
        return Stream.of(santanderPaymentProviderV15);
    }
}