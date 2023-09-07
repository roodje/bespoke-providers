package com.yolt.providers.openbanking.ais.barclaysgroup.pis;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysApp;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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

import static org.assertj.core.api.Assertions.*;

/**
 * This test contains all payment flows ending with payment rejection in Barclays.
 * <p>
 * Covered flows:
 * - when creating payment ends with rejection status we throw CreationFailedException
 * - when created payment is incorrect we throw CreationFailedException
 * - when confirming payment ends with rejection status we throw ConfirmationFailedException
 * - when authorization fails with Access Denied we end with PaymentCancelledException
 * - when authorization fails with server error in url we end with ConfirmationFailedException
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/barclaysgroup/pis-3.1/v3/rejected-payment", httpsPort = 0, port = 0)
@ActiveProfiles("barclays")
public class BarclaysPaymentProviderUnhappyFlowUkDomesticPaymentsIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545";
    private static final String AUTHORIZATION_CODE = "?code=gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
    private static final String EXAMPLE_PROVIDER_STATE = "{\"consentId\":\"BARCLAYS-P-10000002814216\",\"paymentType\":\"SINGLE\",\"openBankingPayment\":\"{\\\"InstructionIdentification\\\":\\\"20201202002028103-4b1t742n-102\\\",\\\"EndToEndIdentification\\\":\\\"B7F2761C\\\",\\\"InstructedAmount\\\":{\\\"Amount\\\":\\\"10000.00\\\",\\\"Currency\\\":\\\"GBP\\\"},\\\"DebtorAccount\\\":{\\\"SchemeName\\\":\\\"UK.OBIE.SortCodeAccountNumber\\\",\\\"Identification\\\":\\\"20581634112471\\\",\\\"Name\\\":\\\"Al Pacino\\\"},\\\"CreditorAccount\\\":{\\\"SchemeName\\\":\\\"UK.OBIE.SortCodeAccountNumber\\\",\\\"Identification\\\":\\\"98765432104322\\\",\\\"Name\\\":\\\"Robert De Niro\\\"},\\\"RemittanceInformation\\\":{\\\"Unstructured\\\":\\\"Unstructured\\\"}}\"}";
    private static final String STATE = "123state123";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final Signer SIGNER = new SignerMock();

    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);
    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("BarclaysPaymentProviderV16")
    private GenericBasePaymentProviderV2 barclaysPaymentProviderV16;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private Stream<GenericBasePaymentProviderV2> getProviders() {
        return Stream.of(barclaysPaymentProviderV16);
    }

    private Stream<Arguments> getProvidersWithSpecificErrors() {
        return Stream.of(
                Arguments.of(barclaysPaymentProviderV16, AccountIdentifierScheme.IBAN, "Barclays Bank", "B7F2761C"),
                Arguments.of(barclaysPaymentProviderV16, AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                        "Name longer than 18", "B7F2761C"),
                Arguments.of(barclaysPaymentProviderV16, AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Barclays Bank",
                        "Too long EndToEndIdentification."));
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new BarclaysSampleTypedAuthenticationMeans().getAuthenticationMean();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d0a9b85f-9715-4d16-a33d-4323ceab5254");
    }

    @ParameterizedTest
    @MethodSource({"getProvidersWithSpecificErrors"})
    public void shouldThrowExceptionForInvalidPaymentRequestWhileTryingToCreateUkDomesticPayment(GenericBasePaymentProviderV2 subject,
                                                                                                 AccountIdentifierScheme scheme,
                                                                                                 String debtorName,
                                                                                                 String endToEndIdentification) {
        //given
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                createRejectInitiateRequestDTO(scheme, debtorName, endToEndIdentification),
                REDIRECT_URL,
                STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> subject.initiateSinglePayment(paymentRequest);
        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(throwingCallable)
                .withCauseExactlyInstanceOf(IllegalArgumentException.class)
                .withMessage("request_creation_error");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnResultWithStatusRejectedForCreatePaymentWhenStatusRejectedFromDomesticPaymentConsentsEndpoint(GenericBasePaymentProviderV2 subject) {
        // given
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                createRejectInitiateRequestDTO(AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Barclays Bank", "B7F2761C"),
                REDIRECT_URL,
                STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
        // when
        InitiateUkDomesticPaymentResponseDTO result = subject.initiateSinglePayment(paymentRequest);

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
    @MethodSource("getProviders")
    public void shouldReruntRejectedResponseWhenSubmitUkDomesticPaymentHaveRejectedStatusInResponse(GenericBasePaymentProviderV2 subject) {
        //given
        SubmitPaymentRequest request = new SubmitPaymentRequest(EXAMPLE_PROVIDER_STATE,
                authenticationMeans,
                REDIRECT_URL + AUTHORIZATION_CODE,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
        // when
        PaymentStatusResponseDTO result = subject.submitPayment(request);
        // then
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("Rejected");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionExceptionWhenAccessDeniedForSubmitUkDomesticPayment(GenericBasePaymentProviderV2 subject) {
        //given
        SubmitPaymentRequest request = new SubmitPaymentRequest(EXAMPLE_PROVIDER_STATE,
                authenticationMeans,
                REDIRECT_URL + "?error=access_denied",
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
        // given
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> subject.submitPayment(request);
        // then
        assertThatThrownBy(throwingCallable).isExactlyInstanceOf(PaymentExecutionTechnicalException.class);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionWhenUnknownDeniedForSubmitUkDomesticPayment(GenericBasePaymentProviderV2 subject) {
        //given
        SubmitPaymentRequest request = new SubmitPaymentRequest(EXAMPLE_PROVIDER_STATE,
                authenticationMeans,
                REDIRECT_URL + "?error=some_other_error",
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
        // when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> subject.submitPayment(request);
        // then
        assertThatThrownBy(throwingCallable).isExactlyInstanceOf(PaymentExecutionTechnicalException.class)
                .hasMessage("submit_preparation_error");
    }

    private InitiateUkDomesticPaymentRequestDTO createRejectInitiateRequestDTO(AccountIdentifierScheme scheme,
                                                                               String debtorName,
                                                                               String endToEndIdentification
    ) {
        UkAccountDTO debtorAccount = new UkAccountDTO("20581634112471",
                scheme,
                debtorName,
                null);
        UkAccountDTO creditorAccount = new UkAccountDTO("98765432104322",
                scheme,
                "Robin",
                null);
        return new InitiateUkDomesticPaymentRequestDTO(
                endToEndIdentification,
                CurrencyCode.GBP.toString(),
                new BigDecimal("-10000.00"),
                creditorAccount,
                debtorAccount,
                "Unstructured",
                new HashMap<>()
        );
    }
}