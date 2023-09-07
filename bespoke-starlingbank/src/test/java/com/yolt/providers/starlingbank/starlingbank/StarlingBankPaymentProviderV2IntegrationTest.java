package com.yolt.providers.starlingbank.starlingbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.starlingbank.SampleAuthenticationMeans;
import com.yolt.providers.starlingbank.TestApp;
import com.yolt.providers.starlingbank.TestSigner;
import com.yolt.providers.starlingbank.common.StarlingBankPaymentProviderV2;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providershared.ProviderPayment;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/starlingbank/v3", files = "classpath:/starlingbank", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestApp.class})
class StarlingBankPaymentProviderV2IntegrationTest {

    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE = "https://www.yolt.com/callback/payment?code=auth-code-payments&state=209c843f-32b3-410f-8b1e-646ba3ff6271";
    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE_WITHOUT_CODE = "https://www.yolt.com/callback/payment?state=209c843f-32b3-410f-8b1e-646ba3ff6271";
    private static final String REDIRECT_URL = "http://yolt.com/callback/starlingbank";
    private static final String STATE = "state";
    private static final String USER_ID_HEADER_NAME = "user-id";
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

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        authenticationMeans = new SampleAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnAuthorizeInfoWithAuthorizeUrlForGenerateAuthorizeUrlForUkDomesticPaymentWithCorrectRequestData(StarlingBankPaymentProviderV2 paymentProvider) throws CreationFailedException {
        // given
        InitiateUkDomesticPaymentRequest initiateUkDomesticPaymentRequest = new InitiateUkDomesticPaymentRequest(
                createUkPaymentRequest(),
                REDIRECT_URL,
                STATE,
                authenticationMeans,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())
        );

        // when
        InitiateUkDomesticPaymentResponseDTO response = paymentProvider.initiateSinglePayment(initiateUkDomesticPaymentRequest);
        String loginUrl = response.getLoginUrl();

        // then
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(loginUrl).contains("/authorize?response_type=code&client_id=api-key&redirect_uri=http://yolt.com/callback/starlingbank&state=state");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnResponseWithExternalPaymentConfirmationIdAndPaymentStatusForConfirmPaymentWithCorrectRequestData(StarlingBankPaymentProviderV2 paymentProvider) throws JsonProcessingException {
        // given
        MDC.put(USER_ID_HEADER_NAME, UUID.randomUUID().toString());
        UkProviderState ukProviderState = new UkProviderState(
                PAYMENT_ID,
                PaymentType.SINGLE,
                createUkPaymentRequest()
        );

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest(
                objectMapper.writeValueAsString(ukProviderState),
                authenticationMeans,
                REDIRECT_URL_POSTED_BACK_FROM_SITE,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()));

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(response.getPaymentId()).isEqualTo("33443344-3344-3344-3344-334433443344");
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).isNotNull();
        assertThat(response.getPaymentExecutionContextMetadata().getPaymentStatuses()).extracting(
                PaymentStatuses::getPaymentStatus,
                (paymentStatuses) -> paymentStatuses.getRawBankPaymentStatus().getStatus(),
                (paymentStatuses) -> paymentStatuses.getRawBankPaymentStatus().getReason())
                .contains(EnhancedPaymentStatus.ACCEPTED, "ACCEPTED", "" );
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnResponseForConfirmPaymentWithoutAuthorizationCode(StarlingBankPaymentProviderV2 paymentProvider) throws JsonProcessingException {
        // given
        MDC.put(USER_ID_HEADER_NAME, UUID.randomUUID().toString());
        UkProviderState ukProviderState = new UkProviderState(
                PAYMENT_ID,
                PaymentType.SINGLE,
                createUkPaymentRequest()
        );

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest(
                objectMapper.writeValueAsString(ukProviderState),
                authenticationMeans,
                REDIRECT_URL_POSTED_BACK_FROM_SITE_WITHOUT_CODE,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()));

        // when
        PaymentStatusResponseDTO response = paymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(response.getProviderState()).isNotEmpty();
        assertThat(response.getPaymentExecutionContextMetadata()).isNotNull();
        assertThat(response.getPaymentExecutionContextMetadata().getPaymentStatuses()).extracting(
                        PaymentStatuses::getPaymentStatus,
                        (paymentStatuses) -> paymentStatuses.getRawBankPaymentStatus().getStatus(),
                        (paymentStatuses) -> paymentStatuses.getRawBankPaymentStatus().getReason())
                .contains(EnhancedPaymentStatus.NO_CONSENT_FROM_USER, "REJECTED", "Missing authorization code in redirect url query parameters");
    }

    private InitiateUkDomesticPaymentRequestDTO createUkPaymentRequest() {
        return new InitiateUkDomesticPaymentRequestDTO(
                "endToEndIdentification",
                CurrencyCode.PLN.name(),
                new BigDecimal("123.11"),
                new UkAccountDTO(
                        "60837188888887",
                        AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                        "Michal Dziewanowski",
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


