package com.yolt.providers.openbanking.ais.barclaysgroup.pis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysApp;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all payment happy flows occuring in Barclays.
 * <p>
 * Covered flows:
 * - successful return of consent page url
 * - successful creation of payment
 * - successful confirmation of payment
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/barclaysgroup/pis-3.1/v3/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("barclays")
public class BarclaysPaymentProviderHappyFlowUkDomesticPaymentsIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545";
    private static final String AUTHORIZATION_CODE = "?code=gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
    private static final String STUBBED_PAYMENT_AUTHORIZE_URL = "https://personalBarclays.com/as/authorization.oauth2\\?response_type=code\\+id_token&client_id=(.*)&state=(.*)&scope=openid\\+payments&nonce=(.*)&redirect_uri=(.*)&request=(.*)";
    private static final String EXAMPLE_PROVIDER_STATE_WITH_SORTCODE = "{\"consentId\":\"BARCLAYS-P-10000002814216\",\"paymentType\":\"SINGLE\",\"openBankingPayment\":\"{\\\"InstructionIdentification\\\":\\\"20201202002028103-4b1t742n-102\\\",\\\"EndToEndIdentification\\\":\\\"B7F2761C\\\",\\\"InstructedAmount\\\":{\\\"Amount\\\":\\\"10000.00\\\",\\\"Currency\\\":\\\"GBP\\\"},\\\"DebtorAccount\\\":{\\\"SchemeName\\\":\\\"UK.OBIE.SortCodeAccountNumber\\\",\\\"Identification\\\":\\\"20581634112471\\\",\\\"Name\\\":\\\"Al Pacino\\\"},\\\"CreditorAccount\\\":{\\\"SchemeName\\\":\\\"UK.OBIE.SortCodeAccountNumber\\\",\\\"Identification\\\":\\\"98765432104322\\\",\\\"Name\\\":\\\"Robert De Niro\\\"},\\\"RemittanceInformation\\\":{\\\"Unstructured\\\":\\\"Unstructured\\\"}}\"}";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STATE = "123state123";
    private static final UUID CLIENT_ID_YOLT = UUID.fromString("297ecda4-fd60-4999-8575-b25ad23b249c");
    private static final UUID CLIENT_REDIRECT_URL_ID_YOLT_APP = UUID.fromString("cee03d67-664c-45d1-b84d-eb042d88ce65");
    private static final Signer SIGNER = new SignerMock();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RestTemplateManagerMock restTemplateManagerMock;
    private AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(CLIENT_ID_YOLT, CLIENT_REDIRECT_URL_ID_YOLT_APP);

    @Autowired
    @Qualifier("BarclaysPaymentProviderV16")
    private GenericBasePaymentProviderV2 barclaysPaymentProviderV16;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private Stream<GenericBasePaymentProviderV2> getProviders() {
        return Stream.of(barclaysPaymentProviderV16);
    }

    private Stream<Arguments> getProviderWithStates() {
        return Stream.of(Arguments.of(barclaysPaymentProviderV16, EXAMPLE_PROVIDER_STATE_WITH_SORTCODE));
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new BarclaysSampleTypedAuthenticationMeans().getAuthenticationMean();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "d0a9b85f-9715-4d16-a33d-4323ceab5254");
    }

    @ParameterizedTest
    @MethodSource({"getProviders"})
    public void shouldCreateUkDomesticPayment(GenericBasePaymentProviderV2 subject) {
        //given
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                createSampleInitiateRequestDTO("Robert De Niro", "Al Pacino", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER),
                REDIRECT_URL,
                STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
        //when
        InitiateUkDomesticPaymentResponseDTO response = subject.initiateSinglePayment(paymentRequest);
        //then
        assertThat(response.getLoginUrl()).matches(STUBBED_PAYMENT_AUTHORIZE_URL);
        String providerState = """
                {"consentId":"BARCLAYS-P-10000002814216","paymentType":"SINGLE",\
                "openBankingPayment":"{\\"InstructionIdentification\\":\\"20201202002028103-4b1t742n-102\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\
                \\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\
                \\"DebtorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"20581634112471\\",\\"Name\\":\\"Al Pacino\\"},\
                \\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"98765432104322\\",\\"Name\\":\\"Robert De Niro\\"},\
                \\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\"}}"}\
                """;
        assertThat(response.getProviderState()).isEqualTo(providerState);
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource({"getProviders"})
    public void shouldCreateUkDomesticPaymentWithoutDebtorProvided(GenericBasePaymentProviderV2 subject) {
        //given
        InitiateUkDomesticPaymentRequest paymentRequest = new InitiateUkDomesticPaymentRequest(
                createSampleInitiateRequestDTOWithoutDebtor("Robert De Niro", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER),
                REDIRECT_URL,
                STATE,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
        //when
        InitiateUkDomesticPaymentResponseDTO response = subject.initiateSinglePayment(paymentRequest);
        //then
        assertThat(response.getLoginUrl()).matches(STUBBED_PAYMENT_AUTHORIZE_URL);
        String providerState = """
                {"consentId":"BARCLAYS-P-10000002814216","paymentType":"SINGLE",\
                "openBankingPayment":"{\\"InstructionIdentification\\":\\"20201202002028103-4b1t742n-102\\",\\"EndToEndIdentification\\":\\"B7F2761C\\",\
                \\"InstructedAmount\\":{\\"Amount\\":\\"10000.00\\",\\"Currency\\":\\"GBP\\"},\
                \\"CreditorAccount\\":{\\"SchemeName\\":\\"UK.OBIE.SortCodeAccountNumber\\",\\"Identification\\":\\"98765432104322\\",\\"Name\\":\\"Robert De Niro\\"},\
                \\"RemittanceInformation\\":{\\"Unstructured\\":\\"Unstructured\\"}}"}\
                """;
        assertThat(response.getProviderState()).isEqualTo(providerState);
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviderWithStates")
    public void shouldSubmitUkDomesticPayment(GenericBasePaymentProviderV2 subject, String state) {
        //given
        SubmitPaymentRequest request = new SubmitPaymentRequest(state,
                authenticationMeans,
                REDIRECT_URL + AUTHORIZATION_CODE,
                SIGNER,
                restTemplateManagerMock,
                PSU_IP_ADDRESS,
                authenticationMeansReference);
        //when
        PaymentStatusResponseDTO response = subject.submitPayment(request);
        assertThat(response.getPaymentId()).isEqualTo("BARCLAYS-P-10000002814216");
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementInProcess");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }


    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnResponseWithConsentIdAndPecMetadataWithInitiationSuccessStatusForGetStatusWhenPaymentIdIsNotProvidedInRequest(GenericBasePaymentProviderV2 subject) throws JsonProcessingException {
        //when
        GetStatusRequest getStatusRequest = creteUkDomesticGetStatusRequest();

        //then
        PaymentStatusResponseDTO response = subject.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEmpty();

        UkProviderState state = OBJECT_MAPPER.readValue(response.getProviderState(), UkProviderState.class);
        assertThat(state).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment).
                contains("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, """
                        {"Status":"AwaitingAuthorisation","resourceId":"331d76df48ed41229b67f062dd55e340"}""");

        assertThat(response.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AwaitingAuthorisation");
                });
    }

    private GetStatusRequest creteUkDomesticGetStatusRequest() {
        return new GetStatusRequest(createUkProviderState(
                new UkProviderState("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, "")),
                null,
                authenticationMeans,
                SIGNER,
                restTemplateManagerMock,
                null,
                authenticationMeansReference);
    }

    private String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO(String creditorName, String debtorName, AccountIdentifierScheme scheme) {
        UkAccountDTO debtorAccount = new UkAccountDTO("20581634112471",
                scheme,
                debtorName,
                null);
        UkAccountDTO creditorAccount = new UkAccountDTO("98765432104322",
                scheme,
                creditorName,
                null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "B7F2761C",
                CurrencyCode.GBP.toString(),
                new BigDecimal("10000.00"),
                creditorAccount,
                debtorAccount,
                "Unstructured",
                new HashMap<>()
        );
    }

    private InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTOWithoutDebtor(String creditorName, AccountIdentifierScheme scheme) {
        UkAccountDTO creditorAccount = new UkAccountDTO("98765432104322",
                scheme,
                creditorName,
                null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "B7F2761C",
                CurrencyCode.GBP.toString(),
                new BigDecimal("10000.00"),
                creditorAccount,
                null,
                "Unstructured",
                new HashMap<>()
        );
    }
}
