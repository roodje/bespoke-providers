package com.yolt.providers.rabobank;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.mock.RestTemplateManagerMock;
import com.yolt.providers.mock.SignerMock;
import com.yolt.providers.rabobank.pis.RabobankPaymentProvider;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy flows occurring in Rabobank group providers.
 * <p>
 * Disclaimer: The group consists of only one {@link RabobankPaymentProvider} provider which is used for testing
 * <p>
 * Covered flows:
 * - payment initiation
 * - payment submission
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/rabobank-pis-1.3.6/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("rabobank")
class RabobankPaymentProviderHappyFlowIntegrationTest {

    private static final String STATE = "8a456132-6b95-44dd-9d09-1ce389e2e392";

    @Autowired
    private RabobankPaymentProvider rabobankPaymentProvider;

    private RestTemplateManagerMock restTemplateManagerMock;

    private SignerMock signerMock = new SignerMock();

    private RabobankSampleTypedAuthenticationMeans sampleAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    void initialize() throws Exception {
        authenticationMeans = sampleAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock();
    }

    Stream<SepaPaymentProvider> getRabobankProviders() {
        return Stream.of(rabobankPaymentProvider);
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnLoginUrlAndStateWithLoginUrlForInitiatePaymentWithCorrectRequestData(SepaPaymentProvider paymentProvider) {
        // given
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setCreditorPostalCountry("NL");
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "GB32ESSE40486562136016"))
                .creditorName("Mr. Smith")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "GB32ESSE40486562136017"))
                .endToEndIdentification("e2e")
                .instructedAmount(new SepaAmountDTO(BigDecimal.TEN))
                .remittanceInformationUnstructured("Test Remittance Info Unstructured")
                .dynamicFields(dynamicFields)
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO).setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("127.0.0.1")
                .setState(STATE)
                .build();

        // when
        LoginUrlAndStateDTO result = paymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("https://betalen.rabobank.nl/afronden-web/deeps/deeplink/deeplink/pi/ucp/single-credit-transfers/start?paymentinitiationid=2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a");
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACTC");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnResponseWithPaymentIdAndAcceptedStatusForSubmitPaymentWithCorrectRequestData(SepaPaymentProvider paymentProvider) {
        // given
        String providerState = """
                {"paymentId":"2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a"}""";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(providerState)
                .setAuthenticationMeans(authenticationMeans)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment")
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("127.0.0.1")
                .build();

        //when
        SepaPaymentStatusResponseDTO result = paymentProvider.submitPayment(submitPaymentRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo("2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RCVD");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnResponseWithPaymentIdAndProperStatusInPecMetadataForGetStatusWhenPaymentIdIsProvidedInRequest(SepaPaymentProvider paymentProvider) {
        // given
        GetStatusRequest getStatusRequest = createGetStatusRequest(true);

        // when
        SepaPaymentStatusResponseDTO result = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a");
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RCVD");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnResponseWithPaymentIdAndProperStatusInPecMetadataForGetStatusWhenPaymentIdIsProvidedInProviderState(SepaPaymentProvider paymentProvider) {
        // given
        GetStatusRequest getStatusRequest = createGetStatusRequest(false);

        // when
        SepaPaymentStatusResponseDTO result = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a");
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RCVD");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    private GetStatusRequest createGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequestBuilder()
                .setPaymentId(withPaymentId ? "2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a" : null)
                .setProviderState(withPaymentId ? null : """
                        {"paymentId":"2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a"}""")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("127.0.0.1")
                .build();
    }
}
