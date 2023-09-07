package com.yolt.providers.openbanking.ais.lloydsbankinggroup.pis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
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
import com.yolt.providers.openbanking.ais.utils.JwtHelper;
import com.yolt.providers.openbanking.ais.utils.UriHelper;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment happy flows occuring in LBG.
 * <p>
 * Disclaimer: as all providers in LBG group are the same from code and stubs perspective (the only difference is configuration)
 * we are running parametrized tests for testing, so we'll cover all payment providers from LBG group
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
@SpringBootTest(classes = {LloydsGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lloydsgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/lloydsbankinggroup/pis/", httpsPort = 0, port = 0)
public class LloydsGroupPaymentProviderHappyFlowIntegrationTest {

    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    private ObjectMapper objectMapper;

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
        return Stream.of(bankOfScotlandPaymentProviderV11,
                halifaxPaymentProviderV11,
                lloydsBankPaymentProviderV11,
                mbnaPaymentProviderV11
        );
    }

    private Stream<PaymentSubmissionProvider> getPecAwarePaymentSubmissionProviders() {
        return Stream.of(bankOfScotlandPaymentProviderV11,
                halifaxPaymentProviderV11,
                lloydsBankPaymentProviderV11,
                mbnaPaymentProviderV11
        );
    }

    private Stream<Arguments> getDataProvidersWithExpectedAudience() {
        return Stream.of(
                Arguments.of(bankOfScotlandPaymentProviderV11, "bos"),
                Arguments.of(halifaxPaymentProviderV11, "hfx"),
                Arguments.of(lloydsBankPaymentProviderV11, "lyds"),
                Arguments.of(mbnaPaymentProviderV11, "mbn")
        );
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
    @MethodSource("getDataProvidersWithExpectedAudience")
    void shouldReturnFAPICompliantLoginUrl(UkDomesticPaymentProvider paymentProvider, String expectedAudience) throws CreationFailedException, MalformedClaimException {
        //given
        InitiateUkDomesticPaymentRequestDTO requestDto = createSimpleInitiateRequestDto();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDto,
                "https://www.yolt.com/callback/payment",
                "testState",
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                "127.0.0.1",
                authenticationMeansReference
        );
        NumericDate expirationDate = NumericDate.now();
        expirationDate.addSeconds(3600);

        //when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        //then
        String authorizeUrl = response.getLoginUrl();
        assertThat(authorizeUrl).isNotEmpty();

        Map<String, String> queryParams = UriHelper.extractQueryParams(authorizeUrl);
        JwtClaims jwtClaims = JwtHelper.parseJwtClaims(queryParams.get("request"));

        assertThat(jwtClaims.getIssuer()).isEqualTo("a4f99159-cb97-4667-b82e-553e8ad8a632");
        assertThat(jwtClaims.getAudience()).containsOnly(expectedAudience);
        assertThat(jwtClaims.getExpirationTime()).isNotNull();
        assertThat(jwtClaims.getExpirationTime().getValue()).isGreaterThanOrEqualTo(expirationDate.getValue());
        assertThat(queryParams)
                .containsOnlyKeys("nonce", "response_type", "client_id", "scope", "state", "redirect_uri", "request")
                .hasEntrySatisfying("nonce", nonce ->
                        assertThat(nonce)
                                .isEqualTo("testState".substring(0, 8))
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "nonce")))
                .hasEntrySatisfying("response_type", responseType ->
                        assertThat(responseType)
                                .isEqualTo("code+id_token")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "response_type").replace(" ", "+")))
                .hasEntrySatisfying("client_id", clientId ->
                        assertThat(clientId)
                                .isEqualTo("a4f99159-cb97-4667-b82e-553e8ad8a632")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "client_id")))
                .hasEntrySatisfying("scope", scope ->
                        assertThat(scope)
                                .isEqualTo("openid+payments")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "scope").replace(" ", "+")))
                .hasEntrySatisfying("state", state ->
                        assertThat(state)
                                .isEqualTo("testState")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "state")))
                .hasEntrySatisfying("redirect_uri", redirectUri ->
                        assertThat(redirectUri)
                                .isEqualTo("https%3A%2F%2Fwww.yolt.com%2Fcallback%2Fpayment")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "redirect_uri").replace(":", "%3A").replace("/", "%2F")));
    }

    @ParameterizedTest
    @MethodSource("getPecAwareUkDomesticPaymentProviders")
    void shouldReturnResponseWithLoginUrlProviderStateAndInitiationSuccessStatusInPecMetadataWhenCorrectDataAreProvided(UkDomesticPaymentProvider paymentProvider) throws CreationFailedException {
        //given
        InitiateUkDomesticPaymentRequestDTO requestDto = createSimpleInitiateRequestDto();
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                requestDto,
                "https://www.yolt.com/callback/payment",
                "testState",
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                "127.0.0.1",
                authenticationMeansReference
        );

        //when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(request);

        //then
        assertThat(response.getLoginUrl()).contains("response_type=code+id_token")
                .contains("client_id=a4f99159-cb97-4667-b82e-553e8ad8a632")
                .contains("state=testState")
                .contains("scope=openid+payments")
                .contains(String.format("nonce=%s", "testState".substring(0, 8)))
                .contains("redirect_uri=https%3A%2F%2Fwww.yolt.com%2Fcallback%2Fpayment")
                .contains("request=");
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                            .contains("AwaitingAuthorisation", "");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPecAwarePaymentSubmissionProviders")
    void shouldReturnResponseWithPaymentIdAndStatusAcceptedInPecMetadataForSubmitPaymentWhenCorrectDataAreProvided(PaymentSubmissionProvider paymentProvider) throws ConfirmationFailedException {
        //given
        String providerState = """
                {"consentId":"bec2bc664f984571b5a20ea666a7d0c1","paymentType":"SINGLE","openBankingPayment":"{\\"InstructionIdentification\\" : \\"2019-01-18 12:32:21.646 - 2513bfeg\\",\\"EndToEndIdentification\\" : \\"35B64F93\\",\\"InstructedAmount\\" : {\\"Amount\\" : \\"0.01\\",\\"Currency\\" : \\"GBP\\"},\\"CreditorAccount\\" : {\\"SchemeName\\" : \\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\" : \\"12345678901234\\", \\"Name\\":\\"P. Jantje\\"},\\"RemittanceInformation\\" : {\\"Reference\\" : \\"SomeRandomMessage\\"}}"}""";
        SubmitPaymentRequest request = createConfirmPaymentRequest(providerState);

        //when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(request);

        //then
        assertThat(response.getPaymentId()).isEqualTo("e23f5d5cd08d44c3993243ad3f19d56e");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                            .contains("AcceptedSettlementInProcess", "");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPecAwarePaymentSubmissionProviders")
    void shouldReturnResponseWithPaymentIdAndCompletedStatusInPecMetadataForGetStatusWhenPaymentIdIsProvidedInRequest(PaymentSubmissionProvider paymentProvider) {
        //given
        GetStatusRequest request = createGetStatusRequest(true);

        //when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(request);

        //then
        assertThat(response.getPaymentId()).isEqualTo("e23f5d5cd08d44c3993243ad3f19d56e");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                            .contains("AcceptedCreditSettlementCompleted", "");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPecAwarePaymentSubmissionProviders")
    void shouldReturnResponseWithConsentIdAndInitiationSuccessStatusInPecMetadataForGetStatusWhenPaymentIdIsNotProvidedInRequest(PaymentSubmissionProvider paymentProvider) throws JsonProcessingException {
        //given
        GetStatusRequest request = createGetStatusRequest(false);

        //when
        PaymentStatusResponseDTO response = paymentProvider.getStatus(request);

        //then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = objectMapper.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"331d76df48ed41229b67f062dd55e340"}""");

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                            .contains("AwaitingAuthorisation", "");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createSimpleInitiateRequestDto() {
        UkAccountDTO creditorAccount = new UkAccountDTO("12345678901234", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "P. Jantje", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "35B64F93",
                CurrencyCode.GBP.toString(),
                new BigDecimal("0.01"),
                creditorAccount,
                null,
                null,
                Collections.singletonMap("remittanceInformationStructured", "Unstructured Remittance Information")
        );
    }

    private SubmitPaymentRequest createConfirmPaymentRequest(String providerState) {
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payments/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0#code=" + authorizationCode,
                signer,
                restTemplateManagerMock,
                "127.0.0.1",
                authenticationMeansReference
        );
    }

    private GetStatusRequest createGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequest(withPaymentId ? null : createUkProviderState(new UkProviderState("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, null)),
                withPaymentId ? "e23f5d5cd08d44c3993243ad3f19d56e" : null,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                "127.0.0.1",
                authenticationMeansReference);
    }

    private String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
