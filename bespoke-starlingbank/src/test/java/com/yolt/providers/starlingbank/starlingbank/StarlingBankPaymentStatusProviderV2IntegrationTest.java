package com.yolt.providers.starlingbank.starlingbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.starlingbank.SampleAuthenticationMeans;
import com.yolt.providers.starlingbank.TestApp;
import com.yolt.providers.starlingbank.TestSigner;
import com.yolt.providers.starlingbank.common.StarlingBankPaymentProviderV2;
import com.yolt.providers.starlingbank.common.model.UkDomesticPaymentProviderState;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providershared.ProviderPayment;
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
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/starlingbank/v3", files = "classpath:/starlingbank", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestApp.class})
class StarlingBankPaymentStatusProviderV2IntegrationTest {

    private static final String PAYMENT_ID = "33443344-3344-3344-3344-334433443344";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Autowired
    @Qualifier("StarlingBankPaymentProviderV7")
    private StarlingBankPaymentProviderV2 paymentProviderV7;

    private Stream<StarlingBankPaymentProviderV2> getProviders() {
        return Stream.of(paymentProviderV7);
    }

    @Autowired
    private TestSigner signer;

    @Autowired
    @Qualifier("StarlingBankObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private Clock clock = Clock.systemDefaultZone();

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        authenticationMeans = new SampleAuthenticationMeans().getAuthenticationMeans();
    }


    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnPaymentStatusResponseWithValidAccessTokenForSubmittedPayment(StarlingBankPaymentProviderV2 paymentProvider) throws JsonProcessingException {
        // given
        UkProviderState ukProviderState = new UkProviderState(
                PAYMENT_ID,
                PaymentType.SINGLE,
                UkDomesticPaymentProviderState.builder()
                        .externalPaymentId(PAYMENT_ID)
                        .accessTokenExpiresIn(Date.from(Instant.now(clock).plusSeconds(300)))
                        .paymentRequest(createUkPaymentRequest())
                        .refreshToken("RefreshToken")
                        .accessToken("AccessToken")
                        .build()
        );

        GetStatusRequest request = new GetStatusRequest(
                objectMapper.writeValueAsString(ukProviderState),
                PAYMENT_ID,
                authenticationMeans,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())

        );
        PaymentStatusResponseDTO response = paymentProvider.getStatus(request);

        // then
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getProviderState())
                .contains("\"paymentType\":\"SINGLE\"" )
                .contains("\"externalPaymentId\":\"33443344-3344-3344-3344-334433443344\"" )
                .contains("\"refreshToken\":\"RefreshToken\"" )
                .contains("\"accessTokenExpiresIn\"" );

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("REJECTED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("SOME_REASON");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnPaymentStatusResponseWithRefreshTokenStepForSubmittedPayment(StarlingBankPaymentProviderV2 paymentProvider) throws JsonProcessingException {
        // given
        UkProviderState ukProviderState = new UkProviderState(
                PAYMENT_ID,
                PaymentType.SINGLE,
                UkDomesticPaymentProviderState.builder()
                        .externalPaymentId(PAYMENT_ID)
                        .accessTokenExpiresIn(Date.from(Instant.now(clock).minusSeconds(301)))
                        .paymentRequest(createUkPaymentRequest())
                        .refreshToken("refresh-token-for-payment")
                        .accessToken("not-relevant-here")
                        .build()
        );

        GetStatusRequest request = new GetStatusRequest(
                objectMapper.writeValueAsString(ukProviderState),
                PAYMENT_ID,
                authenticationMeans,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())

        );
        PaymentStatusResponseDTO response = paymentProvider.getStatus(request);

        // then
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getProviderState())
                .contains("\"paymentType\":\"SINGLE\"" )
                .contains("\"externalPaymentId\":\"33443344-3344-3344-3344-334433443344\"" )
                .contains("\"refreshToken\":\"nextRefreshToken\"" )
                .contains("\"accessTokenExpiresIn\"" );

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACCEPTED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("QUALIFIED_ACCEPT_AFTER_NEXT_WORKING_DAY");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }


    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnInitiatedPaymentStatusWithFixedReasonForGettingPaymentStatusBeforeSubmit(StarlingBankPaymentProviderV2 paymentProvider) throws JsonProcessingException {
        // given
        UkProviderState ukProviderState = new UkProviderState(
                PAYMENT_ID,
                PaymentType.SINGLE,
                UkDomesticPaymentProviderState.builder()
                        .externalPaymentId(PAYMENT_ID)
                        .build()
        );

        GetStatusRequest request = new GetStatusRequest(
                objectMapper.writeValueAsString(ukProviderState),
                PAYMENT_ID,
                authenticationMeans,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())

        );
        PaymentStatusResponseDTO response = paymentProvider.getStatus(request);

        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("UNKNOWN");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("Getting status before submit is not supported by bank");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    private InitiateUkDomesticPaymentRequestDTO createUkPaymentRequest() {
        return new InitiateUkDomesticPaymentRequestDTO(
                "endToEndIdentification",
                CurrencyCode.GBP.name(),
                new BigDecimal("123.11"),
                new UkAccountDTO(
                        "60837188888887",
                        AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                        "John Doe",
                        null),
                null,
                "Payment reference unstructured",
                Collections.singletonMap("remittanceInformationStructured", "reference"));
    }

    private ProviderPayment createPaymentRequest(final String amount) {
        ProviderPayment payment = new ProviderPayment(
                "instructionIdentification",
                "endToEndIdentification",
                null,
                new ProviderAccountNumberDTO(
                        ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER,
                        "60837188888888"),
                CurrencyCode.GBP,
                new BigDecimal(amount),
                "Payment reference",
                null);
        payment.getCreditorAccount().setHolderName("Bob's accounts");
        return payment;
    }
}


