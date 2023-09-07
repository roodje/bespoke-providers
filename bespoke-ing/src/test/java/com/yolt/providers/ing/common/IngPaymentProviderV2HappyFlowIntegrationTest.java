package com.yolt.providers.ing.common;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.pis.common.PeriodicPaymentFrequency;
import com.yolt.providers.common.pis.common.SepaPeriodicPaymentInfo;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.ing.TestApp;
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
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow", httpsPort = 0, port = 0)
public class IngPaymentProviderV2HappyFlowIntegrationTest {

    private RestTemplateManager restTemplateManager;
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("IngFrPaymentProviderV2")
    private IngPaymentProviderV2 ingFrPaymentProvider;

    @Autowired
    @Qualifier("IngItPaymentProviderV2")
    private IngPaymentProviderV2 ingItPaymentProvider;

    @Autowired
    @Qualifier("IngNlPaymentProviderV3")
    private IngPaymentProviderV3 ingNlPaymentProvider;

    @Autowired
    @Qualifier("IngRoPaymentProviderV2")
    private IngPaymentProviderV2 ingRoPaymentProvider;

    @MockBean
    private Clock clock;

    public static final String PERIODIC_PAYMENT_ID = "cc04ef71-085e-468e-87bd-2881d8a03a5e";
    public static final String PERIODIC_STATE = String.format("{\"paymentId\":\"%s\",\"paymentType\":\"PERIODIC\"}", PERIODIC_PAYMENT_ID);
    public static final String SCHEDULED_PAYMENT_ID = "cc04ef71-085e-468e-87bd-2881d8a03a5a";
    public static final String SCHEDULED_STATE = String.format("{\"paymentId\":\"%s\",\"paymentType\":\"SCHEDULED\"}", SCHEDULED_PAYMENT_ID);
    private static final String INITIATE_STATE = "66a32124-b334-4eb8-8700-d6ca9e4410a0";

    private static final Instant CLOCK_INSTANT = Instant.now();

    Stream<SepaPaymentProvider> getIngPaymentProviders() {
        return Stream.of(ingFrPaymentProvider, ingItPaymentProvider, ingNlPaymentProvider, ingRoPaymentProvider);
    }

    Stream<SepaPaymentProvider> getIngScheduledPaymentProviders() {
        return Stream.of(ingNlPaymentProvider);
    }

    Stream<SepaPaymentProvider> getIngPeriodicPaymentProviders() {
        return Stream.of(ingNlPaymentProvider);
    }

    @SneakyThrows
    @BeforeEach
    public void beforeEach() {
        when(clock.instant()).thenReturn(CLOCK_INSTANT);

        authenticationMeans = new IngSampleAuthenticationMeans().getAuthenticationMeans();

        restTemplateManager = new SimpleRestTemplateManagerMock(externalRestTemplateBuilderFactory);

        PrivateKey signingKey = KeyUtil.createPrivateKeyFromPemFormat((loadPemFile("example_client_signing.key")));
        signer = new TestSigner(signingKey);
    }

    @ParameterizedTest
    @MethodSource("getIngPaymentProviders")
    public void shouldReturnLoginUrlAndStateWithLoginUrlForInitiatePaymentWithCorrectRequestData(SepaPaymentProvider ingPaymentProvider) {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164300"))
                .creditorName("Some Name")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164322"))
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("5877.78")))
                .remittanceInformationUnstructured("unstructured-information")
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(INITIATE_STATE)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("111.1.111.111")
                .build();

        // when
        LoginUrlAndStateDTO result = ingPaymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("https://myaccounts.ing.com/payment-initiation/cc04ef71-085e-468e-87bd-2881d8a03a5d/NL/?TPP-Redirect-URI=https://www.yolt.com/callback/payment?state=66a32124-b334-4eb8-8700-d6ca9e4410a0");
    }

    @ParameterizedTest
    @MethodSource("getIngScheduledPaymentProviders")
    public void shouldReturnLoginUrlAndStateWithLoginUrlForInitiateScheduledPaymentWithCorrectRequestData(SepaPaymentProvider ingPaymentProvider) {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164300"))
                .creditorName("Some Name")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164322"))
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("5877.78")))
                .remittanceInformationUnstructured("unstructured-information")
                .executionDate(LocalDate.of(2021, 12, 20))
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(INITIATE_STATE)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("111.1.111.111")
                .build();

        // when
        LoginUrlAndStateDTO result = ingPaymentProvider.initiateScheduledPayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("https://myaccounts.ing.com/payment-initiation/cc04ef71-085e-468e-87bd-2881d8a03a5d/NL/?TPP-Redirect-URI=https://www.yolt.com/callback/payment?state=66a32124-b334-4eb8-8700-d6ca9e4410a0");
        assertThat(result.getProviderState()).isEqualTo(SCHEDULED_STATE);
    }

    @ParameterizedTest
    @MethodSource("getIngPeriodicPaymentProviders")
    public void shouldReturnLoginUrlAndStateWithLoginUrlForInitiatePeriodicPaymentWithCorrectRequestData(SepaPaymentProvider ingPaymentProvider) {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164300"))
                .creditorName("Some Name")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164322"))
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("5877.78")))
                .remittanceInformationUnstructured("unstructured-information")
                .periodicPaymentInfo(SepaPeriodicPaymentInfo.builder()
                        .startDate(LocalDate.of(2021, 12, 20))
                        .endDate(LocalDate.of(2022, 12, 20))
                        .frequency(PeriodicPaymentFrequency.MONTHLY)
                        .build())
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(INITIATE_STATE)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("111.1.111.111")
                .build();

        // when
        LoginUrlAndStateDTO result = ingPaymentProvider.initiatePeriodicPayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("https://myaccounts.ing.com/payment-initiation/cc04ef71-085e-468e-87bd-2881d8a03a5e/NL/?TPP-Redirect-URI=https://www.yolt.com/callback/payment?state=66a32124-b334-4eb8-8700-d6ca9e4410a0");
        assertThat(result.getProviderState()).isEqualTo(PERIODIC_STATE);
    }

    @ParameterizedTest
    @MethodSource("getIngPaymentProviders")
    public void shouldReturnPaymentStatusResponseForSubmitPaymentWithCorrectRequestData(SepaPaymentProvider ingPaymentProvider) {
        // given
        String providerState = "{\"paymentId\":\"cc04ef71-085e-468e-87bd-2881d8a03a5d\"}";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(providerState)
                .setAuthenticationMeans(authenticationMeans).setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?state=66a32124-b334-4eb8-8700-d6ca9e4410a0")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        //when
        SepaPaymentStatusResponseDTO result = ingPaymentProvider.submitPayment(submitPaymentRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo("cc04ef71-085e-468e-87bd-2881d8a03a5d");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("RCVD", "", EnhancedPaymentStatus.INITIATION_SUCCESS));
    }

    @ParameterizedTest
    @MethodSource("getIngPeriodicPaymentProviders")
    public void shouldReturnPaymentStatusResponseForSubmitPeriodicPaymentWithCorrectRequestData(SepaPaymentProvider ingPaymentProvider) {
        // given
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(PERIODIC_STATE)
                .setAuthenticationMeans(authenticationMeans).setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?state=66a32124-b334-4eb8-8700-d6ca9e4410a0")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("111.1.111.111")
                .build();

        //when
        SepaPaymentStatusResponseDTO result = ingPaymentProvider.submitPayment(submitPaymentRequest);

        //then
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("paymentExecutionContextMetadata.rawRequestHeaders", "paymentExecutionContextMetadata.rawResponseHeaders")
                .isEqualTo(createProperPeriodicPaymentStatusResponse());
    }

    @ParameterizedTest
    @MethodSource("getIngPaymentProviders")
    public void shouldReturnPaymentStatusResponseForGetStatusWithCorrectRequestData(SepaPaymentProvider ingPaymentProvider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId("cc04ef71-085e-468e-87bd-2881d8a03a5d")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        //when
        SepaPaymentStatusResponseDTO result = ingPaymentProvider.getStatus(getStatusRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo("cc04ef71-085e-468e-87bd-2881d8a03a5d");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("RCVD", "", EnhancedPaymentStatus.INITIATION_SUCCESS));
    }

    @ParameterizedTest
    @MethodSource("getIngPeriodicPaymentProviders")
    public void shouldReturnPeriodicPaymentStatusResponseForGetStatusWithCorrectRequestData(SepaPaymentProvider ingPaymentProvider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId("cc04ef71-085e-468e-87bd-2881d8a03a5e")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("111.1.111.111")
                .setProviderState(PERIODIC_STATE)
                .build();

        //when
        SepaPaymentStatusResponseDTO result = ingPaymentProvider.getStatus(getStatusRequest);

        //then
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("paymentExecutionContextMetadata.rawRequestHeaders", "paymentExecutionContextMetadata.rawResponseHeaders")
                .isEqualTo(createProperPeriodicPaymentStatusResponse());
    }

    @ParameterizedTest
    @MethodSource("getIngPaymentProviders")
    public void shouldReturnSigningKeyRequirementsForGetSigningKeyRequirements(SepaPaymentProvider ingPaymentProvider) {
        //when
        Optional<KeyRequirements> signingKeyRequirements = ingPaymentProvider.getSigningKeyRequirements();

        //then
        assertThat(signingKeyRequirements).isPresent();
        KeyRequirements keyRequirements = signingKeyRequirements.get();
        assertThat(keyRequirements.getPrivateKidAuthenticationMeanReference()).isEqualTo("signing-key-id");
        assertThat(keyRequirements.getPublicKeyAuthenticationMeanReference()).isEqualTo("signing-certificate");
    }

    @ParameterizedTest
    @MethodSource("getIngPaymentProviders")
    public void shouldReturnTransportKeyRequirementsForGetTransportKeyRequirements(SepaPaymentProvider ingPaymentProvider) {
        //when
        Optional<KeyRequirements> transportKeyRequirements = ingPaymentProvider.getTransportKeyRequirements();

        //then
        assertThat(transportKeyRequirements).isPresent();
        KeyRequirements keyRequirements = transportKeyRequirements.get();
        assertThat(keyRequirements.getPrivateKidAuthenticationMeanReference()).isEqualTo("transport-key-id");
        assertThat(keyRequirements.getPublicKeyAuthenticationMeanReference()).isEqualTo("transport-certificate");
    }

    private String loadPemFile(final String fileName) throws IOException {
        URI uri = resourceLoader.getResource("classpath:certificates/" + fileName).getURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }


    private SepaPaymentStatusResponseDTO createProperPeriodicPaymentStatusResponse() {
        return SepaPaymentStatusResponseDTO.builder()
                .providerState(PERIODIC_STATE)
                .paymentId(PERIODIC_PAYMENT_ID)
                .paymentExecutionContextMetadata(new PaymentExecutionContextMetadata(
                        clock.instant(),
                        clock.instant(),
                        "null",
                        "{\"transactionStatus\":\"ACTV\"}",
                        null,
                        null,
                        new PaymentStatuses(RawBankPaymentStatus.forStatus("ACTV", ""), EnhancedPaymentStatus.ACCEPTED)))
                .build();
    }
}
