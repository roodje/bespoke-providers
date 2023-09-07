package com.yolt.providers.yoltprovider.pis.ukdomestic;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.PaymentValidationException;
import com.yolt.providers.common.exception.dto.DetailedErrorInformation;
import com.yolt.providers.common.exception.dto.FieldName;
import com.yolt.providers.common.pis.common.*;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.yoltprovider.TestApp;
import com.yolt.providers.yoltprovider.YoltPaymentProvider;
import com.yolt.providers.yoltprovider.pis.TestPaymentAuthMeansUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/pis/ukdomestic", files = "classpath:/stubs/pis/ukdomestic", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestApp.class})
public class YoltBankUkPaymentProviderIntegrationTest {

    private static final UUID CLIENT_ID = UUID.fromString("6f16d556-2845-45c4-a3bd-73054dacada5");
    private static final UUID SIGNING_KID = UUID.fromString("87629fe2-0121-4ef9-bc27-b734360ea8fc");
    private static final UUID PUBLIC_KID = UUID.fromString("999b371f-926e-49a9-b23d-e594d5ff47c3");
    private static final String REDIRECT_URL = "http://redirect.url";

    @MockBean
    private Signer signer;

    @Autowired
    private YoltPaymentProvider yoltPaymentProvider;

    @Test
    void shouldReturnInitiateUkDomesticPaymentResponseDTOWithAllFieldsForInitiateSinglePaymentWhenCorrectData() {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                "de955df0-fcee-4c58-a77d-ac4328be1ce7",
                "GBP",
                new BigDecimal("123.45"),
                new UkAccountDTO("1234", AccountIdentifierScheme.IBAN, "Gall Anonim", "4321"),
                new UkAccountDTO("7890", AccountIdentifierScheme.IBAN, "John Kowalsky", "0987"),
                "remittanceUnstructured",
                Collections.emptyMap()
        );
        InitiateUkDomesticPaymentRequest initiateRequest = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );
        JwsSigningResult mockJws = mock(JwsSigningResult.class);
        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(mockJws);
        given(mockJws.getCompactSerialization())
                .willReturn("fakeJws");

        // when
        InitiateUkDomesticPaymentResponseDTO result = yoltPaymentProvider.initiateSinglePayment(initiateRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo(String.format("http://yoltbank.io/authorize?client_id=%s&token=%s&redirect_uri=%s&state=", CLIENT_ID, "fakeJws", REDIRECT_URL));
        assertThat(result.getProviderState()).isEqualTo("{\"paymentType\":\"SINGLE\"}");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("UNKNOWN");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldReturnInitiateUkDomesticPaymentResponseDTOWithAllFieldsForInitiateScheduledPaymentWhenCorrectData() {
        // given
        InitiateUkDomesticScheduledPaymentRequestDTO requestDTO = new InitiateUkDomesticScheduledPaymentRequestDTO(
                "de955df0-fcee-4c58-a77d-ac4328be1ce8",
                "GBP",
                new BigDecimal("123.45"),
                new UkAccountDTO("1234", AccountIdentifierScheme.IBAN, "Gall Anonim", "4321"),
                new UkAccountDTO("7890", AccountIdentifierScheme.IBAN, "John Kowalsky", "0987"),
                "remittanceUnstructured",
                Collections.emptyMap(),
                OffsetDateTime.MAX
        );
        InitiateUkDomesticScheduledPaymentRequest initiateRequest = new InitiateUkDomesticScheduledPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );
        JwsSigningResult mockJws = mock(JwsSigningResult.class);
        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(mockJws);
        given(mockJws.getCompactSerialization())
                .willReturn("fakeJws");

        // when
        InitiateUkDomesticPaymentResponseDTO result = yoltPaymentProvider.initiateScheduledPayment(initiateRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo(String.format("http://yoltbank.io/authorize?client_id=%s&token=%s&redirect_uri=%s&state=", CLIENT_ID, "fakeJws", REDIRECT_URL));
        assertThat(result.getProviderState()).isEqualTo("{\"paymentType\":\"SCHEDULED\"}");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("UNKNOWN");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldReturnInitiateUkDomesticPaymentResponseDTOWithAllFieldsForInitiatePeriodicPaymentWhenCorrectData() {
        // given
        InitiateUkDomesticPeriodicPaymentRequestDTO requestDTO = new InitiateUkDomesticPeriodicPaymentRequestDTO(
                "de955df0-fcee-4c58-a77d-ac4328be1ce9",
                "GBP",
                new BigDecimal("123.45"),
                new UkAccountDTO("1234", AccountIdentifierScheme.IBAN, "Gall Anonim", "4321"),
                new UkAccountDTO("7890", AccountIdentifierScheme.IBAN, "John Kowalsky", "0987"),
                "remittanceUnstructured",
                Collections.emptyMap(),
                new UkPeriodicPaymentInfo(
                        LocalDate.of(2021, 8, 26),
                        LocalDate.of(2022, 8, 26),
                        PeriodicPaymentFrequency.MONTHLY
                )
        );
        InitiateUkDomesticPeriodicPaymentRequest initiateRequest = new InitiateUkDomesticPeriodicPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );
        JwsSigningResult mockJws = mock(JwsSigningResult.class);
        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(mockJws);
        given(mockJws.getCompactSerialization())
                .willReturn("fakeJws");

        // when
        InitiateUkDomesticPaymentResponseDTO result = yoltPaymentProvider.initiatePeriodicPayment(initiateRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo(String.format("http://yoltbank.io/authorize?client_id=%s&token=%s&redirect_uri=%s&state=", CLIENT_ID, "fakeJws", REDIRECT_URL));
        assertThat(result.getProviderState()).isEqualTo("{\"consentId\":\"12324567890\",\"paymentType\":\"PERIODIC\"}");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("UNKNOWN");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldReturnInitiateUkDomesticPaymentResponseDTOWithEmptyLoginUrlAndEmptyProviderStateWithRejectedStatusesForInitiatePaymentWhenPaymentRejected() {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                "de955df0-fcee-4c58-a77d-ac4328be1ce8",
                "GBP",
                new BigDecimal("123.45"),
                new UkAccountDTO("1234", AccountIdentifierScheme.IBAN, "Gall Anonim", "4321"),
                new UkAccountDTO("7890", AccountIdentifierScheme.IBAN, "John Kowalsky", "0987"),
                "remittanceUnstructured",
                Collections.emptyMap()
        );
        InitiateUkDomesticPaymentRequest initiateRequest = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );

        // when
        InitiateUkDomesticPaymentResponseDTO result = yoltPaymentProvider.initiateSinglePayment(initiateRequest);

        // then
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("024");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEqualTo("Consent initiation rejected by bank");
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_ERROR);
                }));
    }

    @Test
    void shouldThrowPaymentValidationExceptionWithDetailedInformationForInitiatePaymentWhenErrorIsClearlyDescribedInBankResponse() {
        // given
        InitiateUkDomesticPaymentRequestDTO requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                "de955df0-fcee-4c58-a77d-ac4328be1cff",
                "GBP",
                new BigDecimal("123.45"),
                new UkAccountDTO("1234", AccountIdentifierScheme.IBAN, "Gall Anonim", "4321"),
                new UkAccountDTO("7890", AccountIdentifierScheme.IBAN, "John Kowalsky", "0987"),
                "remittanceUnstructured",
                Collections.emptyMap()
        );
        InitiateUkDomesticPaymentRequest initiateRequest = new InitiateUkDomesticPaymentRequest(
                requestDTO,
                REDIRECT_URL,
                "",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );
        DetailedErrorInformation expectedInfo = new DetailedErrorInformation(FieldName.ENDTOENDIDENTIFICATION, "^.*{1,20}$");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> yoltPaymentProvider.initiateSinglePayment(initiateRequest);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(callable)
                .withCauseInstanceOf(PaymentValidationException.class)
                .satisfies(
                        e -> assertThat(((PaymentValidationException) e.getCause()).getInfo())
                                .usingRecursiveComparison()
                                .isEqualTo(expectedInfo));

    }

    @Test
    public void shouldReturnPaymentStatusResponseDTOWithAllFieldsForSubmitSinglePaymentWhenCorrectRequestData() {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequest(
                "{\"consentId\":\"12324567890\",\"paymentType\":\"SINGLE\",\"openBankingPayment\":null}",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                "http://yoltbank.io/callback?payment_id=f1f009fb-ec37-4612-a38e-c59c92593094",
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );

        // when
        PaymentStatusResponseDTO result = yoltPaymentProvider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEqualTo("f1f009fb-ec37-4612-a38e-c59c92593094");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementInProcess");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @Test
    public void shouldReturnPaymentStatusResponseDTOWithAllFieldsForSubmitScheduledPaymentWhenCorrectRequestData() {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequest(
                "{\"consentId\":\"12324567891\",\"paymentType\":\"SCHEDULED\",\"openBankingPayment\":null}",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                "http://yoltbank.io/callback?payment_id=f1f009fb-ec37-4612-a38e-c59c92593095",
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );

        // when
        PaymentStatusResponseDTO result = yoltPaymentProvider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEqualTo("f1f009fb-ec37-4612-a38e-c59c92593095");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementInProcess");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @Test
    public void shouldReturnPaymentStatusResponseDTOWithAllFieldsForSubmitPeriodicPaymentWhenCorrectRequestData() {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequest(
                "{\"consentId\":\"12324567892\",\"paymentType\":\"PERIODIC\",\"openBankingPayment\":null}",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                "http://yoltbank.io/callback?payment_id=f1f009fb-ec37-4612-a38e-c59c92593096",
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );

        // when
        PaymentStatusResponseDTO result = yoltPaymentProvider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEqualTo("f1f009fb-ec37-4612-a38e-c59c92593096");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementInProcess");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }));
    }

    @Test
    public void shouldReturnPaymentStatusResponseDTOWithEmptyPaymentIdAndStatusRejectedForSubmitPaymentWhenPaymentRejected() {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequest(
                "{\"paymentType\":\"SINGLE\"}",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                "http://yoltbank.io/callback?payment_id=f1f009fb-ec37-4612-a38e-c59c92593095",
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(CLIENT_ID, UUID.randomUUID())
        );

        // when
        PaymentStatusResponseDTO result = yoltPaymentProvider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("026");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEqualTo("Payment submission rejected by bank");
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.EXECUTION_FAILED);
                }));
    }

    @Test
    public void shouldReturnPaymentStatusResponseDTOWithAllFieldsForGetSinglePaymentStatusWhenCorrectRequestData() {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                "{\"consentId\":\"12324567890\",\"paymentType\":\"SINGLE\",\"openBankingPayment\":null}",
                "f1f009fb-ec37-4612-a38e-c59c92593095",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())
        );

        // when
        PaymentStatusResponseDTO result = yoltPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("f1f009fb-ec37-4612-a38e-c59c92593095");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementCompleted");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @Test
    public void shouldReturnPaymentStatusResponseDTOWithAllFieldsForGetScheduledPaymentStatusWhenCorrectRequestData() {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                "{\"consentId\":\"12324567891\",\"paymentType\":\"SCHEDULED\",\"openBankingPayment\":null}",
                "f1f009fb-ec37-4612-a38e-c59c92593096",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())
        );

        // when
        PaymentStatusResponseDTO result = yoltPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("f1f009fb-ec37-4612-a38e-c59c92593096");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementCompleted");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @Test
    public void shouldReturnPaymentStatusResponseDTOWithAllFieldsForGetPeriodicPaymentStatusWhenCorrectRequestData() {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                "{\"consentId\":\"12324567892\",\"paymentType\":\"PERIODIC\",\"openBankingPayment\":null}",
                "f1f009fb-ec37-4612-a38e-c59c92593097",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())
        );

        // when
        PaymentStatusResponseDTO result = yoltPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("f1f009fb-ec37-4612-a38e-c59c92593097");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetaData ->
                assertThat(pecMetaData.getPaymentStatuses()).satisfies(paymentStatuses -> {
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getStatus()).isEqualTo("AcceptedSettlementCompleted");
                    assertThat(paymentStatuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(paymentStatuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @Test
    public void shouldReturnResponseWithEmptyPaymentIdAndRejectedStatusInPecMetadataForGetStatusWhenBadRequestFromBank() {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequest("{\"paymentType\":\"SINGLE\"}",
                "f1f009fb-ec37-4612-a38e-c59c92593094",
                TestPaymentAuthMeansUtil.getBasicAuthMeans(CLIENT_ID, PUBLIC_KID, SIGNING_KID),
                signer,
                null,
                "10.0.0.1",
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())
        );

        // when
        PaymentStatusResponseDTO result = yoltPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("026");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("Payment submission rejected by bank");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }

}
