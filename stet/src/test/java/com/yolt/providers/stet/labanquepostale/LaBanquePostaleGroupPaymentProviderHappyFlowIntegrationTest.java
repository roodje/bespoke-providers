package com.yolt.providers.stet.labanquepostale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.stet.generic.GenericPaymentProviderV2;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/labanquepostale/pis/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("labanquepostale")
class LaBanquePostaleGroupPaymentProviderHappyFlowIntegrationTest {

    private static final String PAYMENT_ID = "98ff0a93-7f37-41a9-8563-214f898c7b1c";
    private static final String SERIALIZED_PAYMENT_ID = "{\"paymentId\":\"" + PAYMENT_ID + "\"}";

    @Autowired
    private Signer signer;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("LaBanquePostalePaymentProviderV2")
    private GenericPaymentProviderV3 laBanquePostalePaymentProvider;


    private Stream<SepaPaymentProvider> getPaymentProviders() {
        return Stream.of(laBanquePostalePaymentProvider);
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldCreatePayment(GenericPaymentProviderV3 paymentProvider) {
        // given
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setCreditorAgentBic("Creditor Agent Bic");
        dynamicFields.setCreditorAgentName("ING Bank N.V.");

        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .executionDate(LocalDate.now())
                .dynamicFields(dynamicFields)
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7620041010011408742123455"))
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7620041010011408742123456"))
                .creditorName("TEST CREDITOR NAME")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("7.77")))
                .remittanceInformationUnstructured("TEST REMITTANCE INFORMATION")
                .build();

        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setBaseClientRedirectUrl("https://www.yolt.com/callback-acc/payment")
                .setState("TEST_STATE")
                .build();

        // when
        LoginUrlAndStateDTO result = paymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("https://lbp.com/pisp/accueil?origin=tpp&paymentRequestResourceId=98ff0a93-7f37-41a9-8563-214f898c7b1c");
        assertThat(result.getProviderState()).isEqualTo(SERIALIZED_PAYMENT_ID);
        assertThat(result.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("UNKNOWN");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                });
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldConfirmPayment(GenericPaymentProviderV3 paymentProvider) {
        // given
        String jsonProviderState = LaBanquePostaleGroupSampleMeans.createPaymentJsonProviderState(objectMapper, PAYMENT_ID);

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(jsonProviderState)
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback-acc/payment?state=TEST_STATE&psuAuthenticationFactor=JJKJKJ788GKJKJBK")
                .build();

        // when
        SepaPaymentStatusResponseDTO result = paymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(result.getProviderState()).isEqualTo(SERIALIZED_PAYMENT_ID);
        assertThat(result.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.ACSC.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                });
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldGetPaymentStatus(GenericPaymentProviderV3 paymentProvider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId(PAYMENT_ID)
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("")
                .build();

        // when
        SepaPaymentStatusResponseDTO status = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(status.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(status.getProviderState()).isEqualTo(SERIALIZED_PAYMENT_ID);
        assertThat(status.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.ACSC.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                });
    }
}