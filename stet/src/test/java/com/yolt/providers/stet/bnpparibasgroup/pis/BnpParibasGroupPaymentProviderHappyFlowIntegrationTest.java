package com.yolt.providers.stet.bnpparibasgroup.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupTestConfig;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAuthenticationMeans;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BnpParibasGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bnpparibasgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/bnpparibasgroup/pis/happy-flow", httpsPort = 0, port = 0)
public class BnpParibasGroupPaymentProviderHappyFlowIntegrationTest {

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signerMock;

    @Autowired
    @Qualifier("BnpParibasPaymentProviderV2")
    private GenericPaymentProviderV3 bnpParibasPaymentProviderV2;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private static final String PAYMENT_ID = "PAYMENT_REQUEST_RESOURCE_ID";
    private static final String SERIALIZED_PAYMENT_ID = "{\"paymentId\":\"" + PAYMENT_ID + "\"}";

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        authenticationMeans = new BnpParibasGroupSampleAuthenticationMeans().getBnpSampleAuthenticationMeans();
    }

    Stream<SepaPaymentProvider> getBnpParibasGroupPaymentProviders() {
        return Stream.of(bnpParibasPaymentProviderV2);
    }

    @ParameterizedTest
    @MethodSource("getBnpParibasGroupPaymentProviders")
    void shouldInitiatePayment(SepaPaymentProvider paymentProvider) {
        // given
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setDebtorName("John Debtor");

        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL66ABNA9999841234"))
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7630004030160000003778069"))
                .creditorName("myMerchant")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("124.35")))
                .remittanceInformationUnstructured("MyRemittanceInformation")
                .executionDate(LocalDate.now())
                .dynamicFields(dynamicFields)
                .build();
        String baseClientRedirectUrl = "https://www.yolt.com/callback/payment";
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setSigner(signerMock)
                .setBaseClientRedirectUrl(baseClientRedirectUrl)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setState("state")
                .build();

        // when
        LoginUrlAndStateDTO result = paymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("https://psd2-retail.bddf.bnpparibas/paiement/client/payer.html?i=PAYMENT_REQUEST_RESOURCE_ID");
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
    @MethodSource("getBnpParibasGroupPaymentProviders")
    public void shouldConfirmPayment(SepaPaymentProvider provider) {
        // given
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setProviderState(SERIALIZED_PAYMENT_ID)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?state=state&psuAuthenticationFactor=JJKJKJ788GKJKJBK")
                .build();

        // when
        SepaPaymentStatusResponseDTO result = provider.submitPayment(submitPaymentRequest);

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
    @MethodSource("getBnpParibasGroupPaymentProviders")
    public void shouldGetPaymentStatus(SepaPaymentProvider provider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId(PAYMENT_ID)
                .setProviderState(SERIALIZED_PAYMENT_ID)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("")
                .build();

        // when
        SepaPaymentStatusResponseDTO status = provider.getStatus(getStatusRequest);

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
