package com.yolt.providers.rabobank;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.mock.RestTemplateManagerMock;
import com.yolt.providers.mock.SignerMock;
import com.yolt.providers.rabobank.pis.RabobankPaymentProvider;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.assertj.core.api.ThrowableAssert;
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
@AutoConfigureWireMock(stubs = "classpath:/stubs/rabobank-pis-1.3.6/unhappy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("rabobank")
class RabobankPaymentProviderUnhappyFlowIntegrationTest {

    private static final String STATE_400 = "8a456132-6b95-44dd-9d09-1ce389e2e392";
    private static final String STATE_500 = "8a456132-6b95-44dd-9d09-1ce389e2e393";

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
    void shouldReturnResponseWithEmptyLoginUrlAndEmptyProviderStateAndProperStatusInPecMetadataForInitiatePaymentWhenBadRequestReceived(SepaPaymentProvider paymentProvider) {
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
                .setState(STATE_400)
                .build();

        // when
        LoginUrlAndStateDTO result = paymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("CONSENT_EXPIRED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("napwolzewovzegtijaraefcovobonlevesemomhovuwribleechibwugoslirehholsilvidkezetubkuekoimoopuokihecfiluvhekaftusiguilkuinapipbogfiirewejubasujtofnahiptetbeirkawnusarolgawipuvoafwanhuepaedegewiwececonaizezafujobivpailanokoziwasecoceebroudcawonfojupmiirpofuwfesvivalvopujatubamitrulumpig");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnResponseWithEmptyLoginUrlAndEmptyProviderStateAndProperStatusInPecMetadataForInitiatePaymentWhenServerErrorReceived(SepaPaymentProvider paymentProvider) {
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
                .setState(STATE_500)
                .build();

        // when
        LoginUrlAndStateDTO result = paymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("CONSENT_EXPIRED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("napwolzewovzegtijaraefcovobonlevesemomhovuwribleechibwugoslirehholsilvidkezetubkuekoimoopuokihecfiluvhekaftusiguilkuinapipbogfiirewejubasujtofnahiptetbeirkawnusarolgawipuvoafwanhuepaedegewiwececonaizezafujobivpailanokoziwasecoceebroudcawonfojupmiirpofuwfesvivalvopujatubamitrulumpig");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnResponseWithEmptyPaymentIdAndProperStatusInPecMetadataForSubmitPaymentWhenBadRequestReceived(SepaPaymentProvider paymentProvider) {
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
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("CONSENT_EXPIRED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("napwolzewovzegtijaraefcovobonlevesemomhovuwribleechibwugoslirehholsilvidkezetubkuekoimoopuokihecfiluvhekaftusiguilkuinapipbogfiirewejubasujtofnahiptetbeirkawnusarolgawipuvoafwanhuepaedegewiwececonaizezafujobivpailanokoziwasecoceebroudcawonfojupmiirpofuwfesvivalvopujatubamitrulumpig");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnResponseWithEmptyPaymentIdAndProperStatusInPecMetadataForSubmitPaymentWhenServerErrorReceived(SepaPaymentProvider paymentProvider) {
        // given
        String providerState = """
                {"paymentId":"2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8b"}""";
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
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("CONSENT_EXPIRED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("napwolzewovzegtijaraefcovobonlevesemomhovuwribleechibwugoslirehholsilvidkezetubkuekoimoopuokihecfiluvhekaftusiguilkuinapipbogfiirewejubasujtofnahiptetbeirkawnusarolgawipuvoafwanhuepaedegewiwececonaizezafujobivpailanokoziwasecoceebroudcawonfojupmiirpofuwfesvivalvopujatubamitrulumpig");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionForGetStatusWhenBadRequestReceived(SepaPaymentProvider paymentProvider) {
        // given
        GetStatusRequest getStatusRequest = createGetStatusRequest400();

        // when
        SepaPaymentStatusResponseDTO result = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("CONSENT_EXPIRED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("napwolzewovzegtijaraefcovobonlevesemomhovuwribleechibwugoslirehholsilvidkezetubkuekoimoopuokihecfiluvhekaftusiguilkuinapipbogfiirewejubasujtofnahiptetbeirkawnusarolgawipuvoafwanhuepaedegewiwececonaizezafujobivpailanokoziwasecoceebroudcawonfojupmiirpofuwfesvivalvopujatubamitrulumpig");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldThrowPaymentExecutionTechnicalExceptionForGetStatusWhenServerErrorReceived(SepaPaymentProvider paymentProvider) {
        // given
        GetStatusRequest getStatusRequest = createGetStatusRequest500();

        // when
        SepaPaymentStatusResponseDTO result = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("CONSENT_EXPIRED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("napwolzewovzegtijaraefcovobonlevesemomhovuwribleechibwugoslirehholsilvidkezetubkuekoimoopuokihecfiluvhekaftusiguilkuinapipbogfiirewejubasujtofnahiptetbeirkawnusarolgawipuvoafwanhuepaedegewiwececonaizezafujobivpailanokoziwasecoceebroudcawonfojupmiirpofuwfesvivalvopujatubamitrulumpig");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldThrowConfirmationFailedExceptionForSubmitPaymentWhenErrorParameterInRedirectCallback(SepaPaymentProvider paymentProvider) {
        // given
        String providerState = """
                {"paymentId":"2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a"}""";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(providerState)
                .setAuthenticationMeans(authenticationMeans)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?state=123&error=denied")
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("127.0.0.1")
                .build();

        // when
        ThrowableAssert.ThrowingCallable submitPaymentCallable = () -> paymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(submitPaymentCallable)
                .withMessage("submit_preparation_error")
                .withCause(new IllegalStateException("Got error in callback URL. Payment confirmation failed. Redirect url: https://www.yolt.com/callback/payment?state=123&error=denied"));
    }

    private GetStatusRequest createGetStatusRequest400() {
        return new GetStatusRequestBuilder()
                .setPaymentId("2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8a")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("127.0.0.1")
                .build();
    }

    private GetStatusRequest createGetStatusRequest500() {
        return new GetStatusRequestBuilder()
                .setPaymentId("2cdc7c5c-f2ae-4d8f-ac3a-09b26e275a8b")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("127.0.0.1")
                .build();
    }
}
