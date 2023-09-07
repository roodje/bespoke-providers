package com.yolt.providers.stet.boursoramagroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.stet.generic.GenericPaymentProviderV2;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test suite contains all happy flows for Volksbank group PIS provider.
 * Tests is parametrized and run for all providers in group
 * <p>
 * Covered flows:
 * - initiating payment process
 * - submitting payment process (which involves getting for payment status)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BoursoramaGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("boursorama")
@AutoConfigureWireMock(stubs = "classpath:/stubs/boursorama/pis/happy-flow", httpsPort = 0, port = 0)
public class BoursoramaPaymentProviderV3HappyFlowIntegrationTest {

    private static final String BASE_CLIENT_REDIRECT_URL = "https://www.yolt.com/callback/payment";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STATE = "state";
    private static final String PAYMENT_ID = "MyPmtInfId";
    private static final String SERIALIZED_PAYMENT_ID = "{\"paymentId\":\"" + PAYMENT_ID + "\"}";

    private final Signer signer = mock(Signer.class);
    private final Map<String, BasicAuthenticationMean> authenticationMeans = BoursoramaGroupSampleMeans.getAuthMeans();

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("BoursoramaPaymentProviderV3")
    private GenericPaymentProviderV2 boursoramaPaymentProviderV3;

    Stream<SepaPaymentProvider> getBoursoramaProviders() {
        return Stream.of(boursoramaPaymentProviderV3);
    }

    @BeforeEach
    public void setUp() {
        when(signer.sign(ArgumentMatchers.any(byte[].class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(Base64.toBase64String("TEST-ENCODED-SIGNATURE".getBytes()));
    }

    @ParameterizedTest
    @MethodSource("getBoursoramaProviders")
    public void shouldReturnLoginUrlAndStateResponseWithProperLoginAndStateForInitiatePaymentWithCorrectData(SepaPaymentProvider paymentProviderUnderTest) throws JsonProcessingException {
        // given
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setDebtorName("John Debtor");

        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL62ABNA9999841479"))
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7640618802500004082626224"))
                .creditorName("myMerchant")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("124.35")))
                .remittanceInformationUnstructured("Motif du virement")
                .executionDate(LocalDate.of(2020, 1, 1))
                .dynamicFields(dynamicFields)
                .build();

        InitiatePaymentRequest request = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        LoginUrlAndStateDTO loginUrlAndStateDTO = paymentProviderUnderTest.initiatePayment(request);

        // then
        assertThat(loginUrlAndStateDTO.getLoginUrl()).isEqualTo("https://clients.boursorama.com/finalisation-virement/83f9730518ee4bdfa1b024d1642c83c2");
        assertThat(loginUrlAndStateDTO.getProviderState()).isEqualTo("{\"paymentId\":\"83f9730518ee4bdfa1b024d1642c83c2\"}");
        assertThat(loginUrlAndStateDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("UNKNOWN");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                });
    }

    @ParameterizedTest
    @MethodSource("getBoursoramaProviders")
    public void shouldReturnPaymentStatusResponseWithProperPaymentIdAndStatusAcceptedForSubmitPaymentWithCorrectData(SepaPaymentProvider sepaPaymentProvider) {
        // given
        String providerState = BoursoramaGroupSampleMeans.createPaymentJsonProviderState(objectMapper, PAYMENT_ID);
        SubmitPaymentRequest request = new SubmitPaymentRequestBuilder()
                .setProviderState(providerState)
                .setAuthenticationMeans(authenticationMeans)
                .setRedirectUrlPostedBackFromSite(BASE_CLIENT_REDIRECT_URL)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        //when
        SepaPaymentStatusResponseDTO responseDTO = sepaPaymentProvider.submitPayment(request);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(responseDTO.getProviderState()).isEqualTo(SERIALIZED_PAYMENT_ID);
        assertThat(responseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.ACSC.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                });
    }

    @ParameterizedTest
    @MethodSource("getBoursoramaProviders")
    public void shouldReturnPaymentStatusResponseWithPaymentIdAndStatusForGetStatusWhenPaymentIdIsProvidedInRequest(SepaPaymentProvider sepaPaymentProvider) {
        // given
        GetStatusRequest request = new GetStatusRequestBuilder()
                .setPaymentId(PAYMENT_ID)
                .setProviderState(SERIALIZED_PAYMENT_ID)
                .setSigner(signer)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        SepaPaymentStatusResponseDTO responseDTO = sepaPaymentProvider.getStatus(request);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(responseDTO.getProviderState()).isEqualTo(SERIALIZED_PAYMENT_ID);
        assertThat(responseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.ACSC.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                });
    }

    @ParameterizedTest
    @MethodSource("getBoursoramaProviders")
    public void shouldReturnPaymentStatusResponseWithPaymentIdAndStatusForGetStatusWhenPaymentIdIsNotProvidedInRequest(SepaPaymentProvider sepaPaymentProvider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId(null)
                .setProviderState(SERIALIZED_PAYMENT_ID)
                .setSigner(signer)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        SepaPaymentStatusResponseDTO responseDTO = sepaPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(responseDTO.getProviderState()).isEqualTo(SERIALIZED_PAYMENT_ID);
        assertThat(responseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.ACSC.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                });
    }

    @ParameterizedTest
    @MethodSource("getBoursoramaProviders")
    public void shouldReturnPaymentStatusResponseWithPaymentIdAndStatusForGetStatusWhenProviderStateIsNotProvidedInRequest(SepaPaymentProvider sepaPaymentProvider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId(PAYMENT_ID)
                .setProviderState(null)
                .setSigner(signer)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        SepaPaymentStatusResponseDTO responseDTO = sepaPaymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(responseDTO.getProviderState()).isEqualTo(SERIALIZED_PAYMENT_ID);
        assertThat(responseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.ACSC.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                });
    }
}