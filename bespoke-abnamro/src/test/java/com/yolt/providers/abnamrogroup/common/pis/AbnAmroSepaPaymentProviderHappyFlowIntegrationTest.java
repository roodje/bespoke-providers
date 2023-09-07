package com.yolt.providers.abnamrogroup.common.pis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test contains all happy flows for ABN Amro PIS provider.
 * <p>
 * Covered flows:
 * - initiating payment process
 * - submiting payment process (which involves getting for consent info)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0, httpsPort = 0, stubs = "classpath:/wiremock/api_1_2_1/pis/happy_flow")
@ActiveProfiles("test")
public class AbnAmroSepaPaymentProviderHappyFlowIntegrationTest {

    @Autowired
    private AbnAmroPaymentProvider sut;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${wiremock.server.port}")
    private int port;

    private final Clock clock = Clock.system(ZoneId.of("Europe/Amsterdam"));

    @BeforeEach
    public void setUp() {
        when(restTemplateManager.manage(any(RestTemplateManagerConfiguration.class)))
                .thenReturn(restTemplateBuilder
                        .rootUri("http://localhost:" + port)
                        .build());
    }

    @Test
    public void shouldReturnLoginUrlAndStateWithProperLoginUrlForInitiatePaymentWithCorrectData() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL62ABNA9999841479"))
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL12ABNA9999876523"))
                .creditorName("John Doe")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("149.99")))
                .remittanceInformationUnstructured("Payment of invoice 123/01")
                .build();
        String baseClientRedirectUrl = "https://www.yolt.com/callback/payment";
        AbnAmroTestPisAuthenticationMeans testPisAuthenticationMeans = new AbnAmroTestPisAuthenticationMeans();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl(baseClientRedirectUrl)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(testPisAuthenticationMeans.getAuthMeans())
                .setState("state")
                .build();

        // when
        LoginUrlAndStateDTO result = sut.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isNotEmpty();
        UriComponents loginUrlComponents = UriComponentsBuilder.fromHttpUrl(result.getLoginUrl()).build();
        assertThat(loginUrlComponents.getPath()).isEqualTo("/as/authorization.oauth2");
        assertThat(loginUrlComponents.getQueryParams())
                .containsEntry("scope", Collections.singletonList("psd2:payment:sepa:write+psd2:payment:sepa:read"))
                .containsEntry("client_id", Collections.singletonList("TPP_test"))
                .containsEntry("transactionId", Collections.singletonList("8325P3346070108S0PD"))
                .containsEntry("response_type", Collections.singletonList("code"))
                .containsEntry("flow", Collections.singletonList("code"))
                .containsEntry("redirect_uri", Collections.singletonList(baseClientRedirectUrl))
                .containsEntry("state", Collections.singletonList("state"));
        AbnAmroPaymentProviderState providerState = deserializeProviderState(result.getProviderState());
        assertThat(providerState).extracting(AbnAmroPaymentProviderState::getTransactionId, AbnAmroPaymentProviderState::getRedirectUri)
                .contains("8325P3346070108S0PD", "https://www.yolt.com/callback/payment");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("STORED", "", EnhancedPaymentStatus.INITIATION_SUCCESS));
    }

    @Test
    public void shouldReturnSepaPaymentStatusResponseWithPaymentIdAndStatusAcceptedForSubmitPaymentWithCorrectData() {
        // given
        AbnAmroTestPisAuthenticationMeans testPisAuthenticationMeans = new AbnAmroTestPisAuthenticationMeans();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(testPisAuthenticationMeans.getAuthMeans())
                .setProviderState("""
                        {"transactionId":"8325P3346070108S0PD","redirectUri":"https://www.yolt.com/callback/payment"}""")
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?code=9C6UrsGZ0Z3XJymRAOAgl7hKPLlWKUo9GBfMQQEs")
                .build();

        // when
        SepaPaymentStatusResponseDTO result = sut.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("8325P3346070108S0PD");
        AbnAmroPaymentProviderState providerState = deserializeProviderState(result.getProviderState());
        assertThat(providerState).satisfies(state -> {
            assertThat(state.getTransactionId()).isEqualTo("8325P3346070108S0PD");
            assertThat(state.getUserAccessTokenState()).satisfies(userAccessTokenState -> {
                assertThat(userAccessTokenState.getAccessToken()).isEqualTo("GPgYglX4sO1WhzfChx4tmjr4y7Qg");
                assertThat(userAccessTokenState.getRefreshToken()).isEqualTo("UHjIAzBZfLGh4dLm8cvEcH6d8BrOmCZXumOpznQBP1");
                assertThat(userAccessTokenState.getExpirationZonedDateTime()).isBetween(ZonedDateTime.now(clock), ZonedDateTime.now(clock).plus(2, ChronoUnit.HOURS));
            });
        });
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("EXECUTED", "", EnhancedPaymentStatus.COMPLETED));
    }

    @Test
    public void shouldReturnResponseWithTransactionIdAndNullUserAccessTokenAndUnknownStateInPecMetadataForGetStatusWhenUserAccessTokenIsNotProvidedInProviderState() {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans())
                .setPaymentId("8325P3346070108S0PD")
                .setProviderState("""
                        {"transactionId":"8325P3346070108S0PD"}""")
                .build();

        // when
        SepaPaymentStatusResponseDTO result = sut.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("UNKNOWN", "", EnhancedPaymentStatus.UNKNOWN));
    }

    @Test
    public void shouldReturnResponseWithTransactionIdAndUserAccessTokenAndCompletedStateInPecMetadataForGetStatusWhenUserAccessTokenIsProvidedInProviderStateAndAccessTokenIsNotExpired() {
        // given
        String providerStateAsString = serializeProviderState(new AbnAmroPaymentProviderState("8325P3346070108S0PD",
                null,
                new AbnAmroPaymentProviderState.UserAccessTokenState("GPgYglX4sO1WhzfChx4tmjr4y7Qg",
                        "GPgYglX4sO1WhzfChx4tmjr4y7Qh",
                        7200,
                        clock)));
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans())
                .setPaymentId("8325P3346070108S0PD")
                .setProviderState(providerStateAsString)
                .build();

        // when
        SepaPaymentStatusResponseDTO result = sut.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("8325P3346070108S0PD");
        AbnAmroPaymentProviderState providerState = deserializeProviderState(result.getProviderState());
        assertThat(providerState).satisfies(state -> {
            assertThat(state.getTransactionId()).isEqualTo("8325P3346070108S0PD");
            assertThat(state.getUserAccessTokenState()).satisfies(userAccessTokenState -> {
                assertThat(userAccessTokenState.getAccessToken()).isEqualTo("GPgYglX4sO1WhzfChx4tmjr4y7Qg");
                assertThat(userAccessTokenState.getRefreshToken()).isEqualTo("GPgYglX4sO1WhzfChx4tmjr4y7Qh");
                assertThat(userAccessTokenState.getExpirationZonedDateTime()).isBetween(ZonedDateTime.now(), ZonedDateTime.now().plusHours(2));
            });
        });
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("EXECUTED", "", EnhancedPaymentStatus.COMPLETED));
    }

    @Test
    public void shouldReturnResponseWithTransactionIdAndUserAccessTokenAndCompletedStateInPecMetadataForGetStatusWhenUserAccessTokenIsProvidedInProviderStateAndAccessTokenIsExpired() {
        // given
        String providerStateAsString = serializeProviderState(new AbnAmroPaymentProviderState("8325P3346070108S0PD",
                null,
                new AbnAmroPaymentProviderState.UserAccessTokenState("GPgYglX4sO1WhzfChx4tmjr4y7Qg",
                        "GPgYglX4sO1WhzfChx4tmjr4y7Qh",
                        -60,
                        clock)));
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans())
                .setPaymentId("8325P3346070108S0PD")
                .setProviderState(providerStateAsString)
                .build();

        // when
        SepaPaymentStatusResponseDTO result = sut.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("8325P3346070108S0PD");
        AbnAmroPaymentProviderState providerState = deserializeProviderState(result.getProviderState());
        assertThat(providerState).satisfies(state -> {
            assertThat(state.getTransactionId()).isEqualTo("8325P3346070108S0PD");
            assertThat(state.getUserAccessTokenState()).satisfies(userAccessTokenState -> {
                assertThat(userAccessTokenState.getAccessToken()).isEqualTo("GPgYglX4sO1WhzfChx4tmjr4y7Qi");
                assertThat(userAccessTokenState.getRefreshToken()).isEqualTo("GPgYglX4sO1WhzfChx4tmjr4y7Qj");
                assertThat(userAccessTokenState.getExpirationZonedDateTime()).isBetween(ZonedDateTime.now(clock), ZonedDateTime.now(clock).plusHours(2));
            });
        });
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("EXECUTED", "", EnhancedPaymentStatus.COMPLETED));
    }

    private AbnAmroPaymentProviderState deserializeProviderState(String providerState) {
        try {
            return new ObjectMapper().readValue(providerState, AbnAmroPaymentProviderState.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String serializeProviderState(AbnAmroPaymentProviderState providerState) {
        try {
            return new ObjectMapper().writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
