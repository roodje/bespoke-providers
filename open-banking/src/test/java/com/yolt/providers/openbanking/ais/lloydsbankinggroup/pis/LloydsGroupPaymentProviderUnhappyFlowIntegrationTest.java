package com.yolt.providers.openbanking.ais.lloydsbankinggroup.pis;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.common.providerinterface.PaymentSubmissionProvider;
import com.yolt.providers.common.providerinterface.UkDomesticPaymentProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsGroupApp;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsSampleTypedAuthenticationMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test contains all payment flows ending with payment rejection in LBG.
 * <p>
 * Disclaimer: as all providers in LBG group are the same from code and stubs perspective (the only difference is configuration)
 * we are running parametrized tests for testing, so we'll cover all payment providers from LBG group
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
@SpringBootTest(classes = {LloydsGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lloydsgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/lloydsbankinggroup/pis/", httpsPort = 0, port = 0)
public class LloydsGroupPaymentProviderUnhappyFlowIntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String TEST_REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String TEST_STATE = "aTestState";
    private static final String END_TO_END_IDENTIFICATION = "FEF32557";
    private static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-100.00");
    private static final BigDecimal POSITIVE_AMOUNT = new BigDecimal("0.01");
    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("BankOfScotlandPaymentProviderV11")
    private GenericBasePaymentProviderV2 bankOfScotlandPaymentProviderV11;
    @Autowired
    @Qualifier("HalifaxPaymentProviderV11")
    private GenericBasePaymentProviderV2 halifaxPaymentProviderV11;
    @Autowired
    @Qualifier("LloydsBankPaymentProviderV11")
    private GenericBasePaymentProviderV2 lloydsBankPaymentProviderV11;
    @Autowired
    @Qualifier("MbnaPaymentProviderV11")
    private GenericBasePaymentProviderV2 mbnaPaymentProviderV11;

    private Stream<UkDomesticPaymentProvider> getPecAwareUkDomesticPaymentProviders() {
        return Stream.of(bankOfScotlandPaymentProviderV11, halifaxPaymentProviderV11, lloydsBankPaymentProviderV11, mbnaPaymentProviderV11);
    }

    private Stream<UkDomesticPaymentProvider> getPecAwarePaymentSubmissionProviders() {
        return Stream.of(bankOfScotlandPaymentProviderV11, halifaxPaymentProviderV11, lloydsBankPaymentProviderV11, mbnaPaymentProviderV11);
    }

    private Signer signer;
    private final AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = new LloydsSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "c554a9ef-47c1-4b4e-a77f-2ad770d69748");
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getPecAwareUkDomesticPaymentProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionWithIllegalArgumentExceptionAsCauseForInitiateSinglePaymentWhenToLongReferenceInformation(UkDomesticPaymentProvider paymentProvider) {
        // given
        InitiateUkDomesticPaymentRequest request = createUkDomesticPaymentRequest(createInitiateUkDomesticPaymentWithToLongRemittanceReferenceStructured());

        // when
        final ThrowableAssert.ThrowingCallable paymentCallable = () -> paymentProvider.initiateSinglePayment(request);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(paymentCallable)
                .withCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("Remittance Information Reference is too long 53. Maximum length for Lloyds Banking Group is 35"));
    }

    @ParameterizedTest
    @MethodSource("getPecAwareUkDomesticPaymentProviders")
    public void shouldReturnResponseWithEmptyLoginUrlAndEmptyProviderStateWithStatusInitiationErrorInPecMetadataForInitiateSinglePaymentWhenBadRequestIsReceivedFromBank(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequest request = createUkDomesticPaymentRequest(createInitiateUkDomesticPaymentWithoutEndToEndIdentification());

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        // then
        assertThat(response.getLoginUrl()).isEmpty();
        assertThat(response.getProviderState()).isEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                            .contains("400 BadRequest", """
                                    [{"ErrorCode":"UK.OBIE.Field.Missing","Message":"End to end identification is missing","Path":"Data.Initiation.InstructionIdentification","Url":"<url to the api reference for Payment Inititaion API>"}]""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPecAwarePaymentSubmissionProviders")
    public void shouldReturnResponseWithEmptyPaymentIdWithStatusExecutionFailedInPecMetadataForSubmitPaymentWhenBadRequestIsReceivedFromBank(PaymentSubmissionProvider paymentProvider) throws ConfirmationFailedException {
        // given
        SubmitPaymentRequest request = createUkDomesticSubmitPaymentRequest();

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        // then
        assertThat(response.getPaymentId()).isEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                            .contains("400 BadRequest", """
                                    [{"ErrorCode":"UK.OBIE.Field.Missing","Message":"Instructed amount does not match","Path":"Data.Initiation.InstructedAmount.Amount","Url":"<url to the api reference for Payment Inititaion API>"}]""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.EXECUTION_FAILED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPecAwarePaymentSubmissionProviders")
    public void shouldReturnResponseWithEmptyPaymentIdWithStatusRejectedInPecMetadataForGetStatusWhenBadRequestIsReceivedFromBank(PaymentSubmissionProvider paymentProvider) {
        // given
        GetStatusRequest request = createUkDomesticGetStatusRequest();

        // when
        PaymentStatusResponseDTO result = paymentProvider.getStatus(request);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("400 BadRequest");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("""
                            [{"ErrorCode":"UK.OBIE.Rules.AfterCutOffDateTime","Message":"{payment-order} consent / resource received after CutOffDateTime","Url":"<url to the api reference for Payment Inititaion API>"}]""");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

    private GetStatusRequest createUkDomesticGetStatusRequest() {
        return new GetStatusRequest(null,
                "e23f5d5cd08d44c3993243ad3f19d56f",
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference);
    }

    private SubmitPaymentRequest createUkDomesticSubmitPaymentRequest() {
        return new SubmitPaymentRequest("""
                {"consentId":"bec2bc664f984571b5a20ea666a7d0c2","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\" : \\"2019-01-18 12:32:21.646 - 2513bfeg\\",\\"EndToEndIdentification\\" : \\"35B64F93\\",\\"InstructedAmount\\" : {\\"Amount\\" : \\"0.01\\",\\"Currency\\" : \\"GBP\\"},\\"CreditorAccount\\" : {\\"SchemeName\\" : \\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\" : \\"12345678901234\\", \\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\" : {\\"Reference\\" : \\"SomeRandomMessage\\"}}"}""",
                authenticationMeans,
                UriComponentsBuilder.fromUriString(TEST_REDIRECT_URL)
                        .queryParam("code", "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA")
                        .build()
                        .toString(),
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference);
    }

    private InitiateUkDomesticPaymentRequest createUkDomesticPaymentRequest(InitiateUkDomesticPaymentRequestDTO requestDTO) {
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

    private InitiateUkDomesticPaymentRequestDTO createInitiateUkDomesticPaymentWithToLongRemittanceReferenceStructured() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                END_TO_END_IDENTIFICATION,
                CurrencyCode.GBP.toString(),
                NEGATIVE_AMOUNT,
                creditorAccount,
                debtorAccount,
                "PaymentToReject",
                Collections.singletonMap("remittanceInformationStructured", "Very long description which has exactly 53 characters")
        );
    }

    private InitiateUkDomesticPaymentRequestDTO createInitiateUkDomesticPaymentWithoutEndToEndIdentification() {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                null,
                CurrencyCode.GBP.toString(),
                POSITIVE_AMOUNT,
                creditorAccount,
                debtorAccount,
                "Unstructured",
                Collections.singletonMap("remittanceInformationStructured", "Reference")
        );
    }
}
