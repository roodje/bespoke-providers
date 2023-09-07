package com.yolt.providers.knabgroup.common;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.knabgroup.TestApp;
import com.yolt.providers.knabgroup.TestRestTemplateManager;
import com.yolt.providers.knabgroup.TestSigner;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/pis/happyflow", httpsPort = 0, port = 0)
public class KnabPaymentProviderV2HappyFlowIntegrationTest {

    private RestTemplateManager restTemplateManager;
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    @Qualifier("KnabPaymentProvider")
    private KnabGroupPaymentProvider knabPaymentProvider;

    @MockBean
    private Clock clock;

    private static final String INITIATE_STATE = "66a32124-b334-4eb8-8700-d6ca9e4410a0";

    private static final Instant CLOCK_INSTANT = Instant.now();

    Stream<SepaPaymentProvider> getPaymentProviders() {
        return Stream.of(knabPaymentProvider);
    }

    @SneakyThrows
    @BeforeEach
    public void beforeEach() {
        when(clock.instant()).thenReturn(CLOCK_INSTANT);
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        signer = new TestSigner();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnLoginUrlAndStateWithLoginUrlForInitiatePaymentWithCorrectRequestData(SepaPaymentProvider paymentProvider) {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164300"))
                .creditorName("Jonas Snow")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL52KNAB9992936932"))
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("10.00")))
                .remittanceInformationUnstructured("Utility bill")
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yoltTestUrl.com")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(INITIATE_STATE)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        // when
        LoginUrlAndStateDTO result = paymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl())
                .isEqualTo("https://api.knab.nl/sandbox-paymentsapi/v1/signing-page-sandbox?id=c7b9461a2d324091ab4b83df8853bb92&type=psd2payment");
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnPaymentStatusResponseForSubmitPaymentWithCorrectRequestData(SepaPaymentProvider paymentProvider) {
        // given
        String providerState = "{\"paymentId\":\"c7b9461a2d324091ab4b83df8853bb92\"}";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(providerState)
                .setAuthenticationMeans(authenticationMeans).setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?state=66a32124-b334-4eb8-8700-d6ca9e4410a0")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        //when
        SepaPaymentStatusResponseDTO result = paymentProvider.submitPayment(submitPaymentRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo("c7b9461a2d324091ab4b83df8853bb92");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("ACCP", "", EnhancedPaymentStatus.COMPLETED));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnPaymentStatusResponseForGetStatusWithCorrectRequestData(SepaPaymentProvider paymentProvider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId("c7b9461a2d324091ab4b83df8853bb92")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        //when
        SepaPaymentStatusResponseDTO result = paymentProvider.getStatus(getStatusRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo("c7b9461a2d324091ab4b83df8853bb92");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("ACCP", "", EnhancedPaymentStatus.COMPLETED));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnSigningKeyRequirementsForGetSigningKeyRequirements(SepaPaymentProvider paymentProvider) {
        //when
        Optional<KeyRequirements> signingKeyRequirements = paymentProvider.getSigningKeyRequirements();

        //then
        assertThat(signingKeyRequirements).isPresent();
        KeyRequirements keyRequirements = signingKeyRequirements.get();
        assertThat(keyRequirements.getPrivateKidAuthenticationMeanReference()).isEqualTo("signing-private-key-id");
        assertThat(keyRequirements.getPublicKeyAuthenticationMeanReference()).isEqualTo("signing-certificate");
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnTransportKeyRequirementsForGetTransportKeyRequirements(SepaPaymentProvider paymentProvider) {
        //when
        Optional<KeyRequirements> transportKeyRequirements = paymentProvider.getTransportKeyRequirements();

        //then
        assertThat(transportKeyRequirements).isPresent();
        KeyRequirements keyRequirements = transportKeyRequirements.get();
        assertThat(keyRequirements.getPrivateKidAuthenticationMeanReference()).isEqualTo("transport-private-key-id");
        assertThat(keyRequirements.getPublicKeyAuthenticationMeanReference()).isEqualTo("transport-certificate");
    }
}
