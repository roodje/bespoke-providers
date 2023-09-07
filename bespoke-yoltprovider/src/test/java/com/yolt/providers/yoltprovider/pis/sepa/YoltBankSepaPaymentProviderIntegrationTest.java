package com.yolt.providers.yoltprovider.pis.sepa;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.common.PeriodicPaymentExecutionRule;
import com.yolt.providers.common.pis.common.PeriodicPaymentFrequency;
import com.yolt.providers.common.pis.common.SepaPeriodicPaymentInfo;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.yoltprovider.TestApp;
import com.yolt.providers.yoltprovider.YoltPaymentProvider;
import com.yolt.providers.yoltprovider.pis.TestPaymentAuthMeansUtil;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/pis/sepa", files = "classpath:/stubs/pis/sepa", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestApp.class})
class YoltBankSepaPaymentProviderIntegrationTest {

    private static final UUID CLIENT_ID = UUID.fromString("6f16d556-2845-45c4-a3bd-73054dacada5");
    private static final UUID SIGNING_KID = UUID.fromString("87629fe2-0121-4ef9-bc27-b734360ea8fc");
    private static final UUID PUBLIC_KID = UUID.fromString("999b371f-926e-49a9-b23d-e594d5ff47c3");
    private static final String REDIRECT_URL = "http://redirect.url";

    @MockBean
    private Signer signer;

    @Autowired
    private YoltPaymentProvider yoltPaymentProvider;

    @Test
    void shouldReturnLoginUrlAndStateDTOWithAllFieldsForInitiateSinglePaymentWhenCorrectData() {
        // given
        InitiatePaymentRequest initiatePaymentRequestDTO = new InitiatePaymentRequestBuilder()
                .setRequestDTO(minimalValidSingleRequest(true))
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState("")
                .setAuthenticationMeans(TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID))
                .setSigner(signer)
                .setRestTemplateManager(null)
                .build();

        // when
        LoginUrlAndStateDTO result = yoltPaymentProvider.initiatePayment(initiatePaymentRequestDTO);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("http://yoltbank.io/authorize");
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"123","paymentType":"SINGLE"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("INITIATED");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldReturnLoginUrlAndStateDTOWithAllFieldsForInitiatePeriodicPaymentWhenCorrectData() {
        // given
        InitiatePaymentRequest initiatePaymentRequestDTO = new InitiatePaymentRequest(
                minimalValidPeriodicRequest(),
                REDIRECT_URL,
                "",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                null,
                null
        );

        // when
        LoginUrlAndStateDTO result = yoltPaymentProvider.initiatePeriodicPayment(initiatePaymentRequestDTO);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("http://yoltbank.io/authorize");
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"123","paymentType":"PERIODIC"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("INITIATED");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldReturnPaymentResponseWithAllFieldsForSubmitSinglePaymentWhenCorrectRequestData() {
        // given
        String state = "{\"paymentId\": \"3acc39d0-3e38-4140-b8c4-ff53c9b0f5d3\", \"paymentType\":\"SINGLE\"}";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(state)
                .setAuthenticationMeans(TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID))
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL)
                .setSigner(signer)
                .build();

        // when
        SepaPaymentStatusResponseDTO result = yoltPaymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("3acc39d0-3e38-4140-b8c4-ff53c9b0f5d3");
        assertThat(result.getProviderState()).isEqualToIgnoringWhitespace(state);
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACCEPTED");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @Test
    void shouldReturnPaymentResponseWithAllFieldsForSubmitPeriodicPaymentWhenCorrectRequestData() {
        // given
        String state = "{\"paymentId\": \"3acc39d0-3e38-4140-b8c4-ff53c9b0f5d5\", \"paymentType\":\"PERIODIC\"}";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(state)
                .setAuthenticationMeans(TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID))
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL)
                .setSigner(signer)
                .build();

        // when
        SepaPaymentStatusResponseDTO result = yoltPaymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("3acc39d0-3e38-4140-b8c4-ff53c9b0f5d5");
        assertThat(result.getProviderState()).isEqualToIgnoringWhitespace(state);
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACCEPTED");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @Test
    void shouldReturnPaymentResponseWithEmptyPaymentIdAndStatusRejectedForSubmitPaymentWhenPaymentRejected() {
        // given
        String state = "{\"paymentId\": \"3acc39d0-3e38-4140-b8c4-ff53c9b0f5d4\", \"paymentType\":\"SINGLE\"}";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(state)
                .setAuthenticationMeans(TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID))
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL)
                .setSigner(signer)
                .build();

        // when
        SepaPaymentStatusResponseDTO result = yoltPaymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getProviderState()).isEqualToIgnoringWhitespace(state);
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("UNKNOWN");
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    @Test
    void shouldReturnResponseWithStatusInitiatedForGetSinglePaymentStatusWithCorrectRequestData() {
        // given
        String state = "{\"paymentId\": \"3acc39d0-3e38-4140-b8c4-ff53c9b0f5d3\", \"paymentType\":\"SINGLE\"}";
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId("3acc39d0-3e38-4140-b8c4-ff53c9b0f5d3")
                .setAuthenticationMeans(TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID))
                .setSigner(signer)
                .setProviderState(state)
                .build();

        // when
        SepaPaymentStatusResponseDTO status = yoltPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(status.getProviderState()).isEqualToIgnoringWhitespace(state);
        assertThat(status.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("INITIATED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldReturnResponseWithStatusInitiatedForGetPeriodicPaymentStatusWithCorrectRequestData() {
        // given
        String state = "{\"paymentId\": \"3acc39d0-3e38-4140-b8c4-ff53c9b0f5d5\", \"paymentType\":\"PERIODIC\"}";
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId("3acc39d0-3e38-4140-b8c4-ff53c9b0f5d5")
                .setAuthenticationMeans(TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID))
                .setSigner(signer)
                .setProviderState(state)
                .build();

        // when
        SepaPaymentStatusResponseDTO status = yoltPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(status.getProviderState()).isEqualToIgnoringWhitespace(state);
        assertThat(status.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("INITIATED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    private static SepaInitiatePaymentRequestDTO minimalValidSingleRequest(boolean successful) {
        return SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(SepaAccountDTO.builder()
                        .iban(successful ? "AB1234" : "AB12345")
                        .currency(CurrencyCode.EUR)
                        .build())
                .creditorName("fake creditor")
                .debtorAccount(SepaAccountDTO.builder()
                        .iban("CD5678")
                        .currency(CurrencyCode.EUR)
                        .build())
                .endToEndIdentification("endToEndIdentification")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(BigDecimal.ONE)
                        .build())
                .executionDate(LocalDate.of(2000, 12, 31))
                .remittanceInformationUnstructured("fake reference")
                .build();
    }

    private static SepaInitiatePaymentRequestDTO minimalValidPeriodicRequest() {
        return SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(SepaAccountDTO.builder()
                        .iban("AB1234")
                        .currency(CurrencyCode.EUR)
                        .build())
                .creditorName("fake creditor")
                .debtorAccount(SepaAccountDTO.builder()
                        .iban("CD5678")
                        .currency(CurrencyCode.EUR)
                        .build())
                .endToEndIdentification("endToEndIdentification")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(BigDecimal.ONE)
                        .build())
                .remittanceInformationUnstructured("fake reference")
                .periodicPaymentInfo(SepaPeriodicPaymentInfo.builder()
                        .startDate(LocalDate.of(2021, 8, 26))
                        .endDate(LocalDate.of(2021, 8, 26))
                        .frequency(PeriodicPaymentFrequency.DAILY)
                        .executionRule(PeriodicPaymentExecutionRule.FOLLOWING)
                        .build())
                .build();
    }
}
