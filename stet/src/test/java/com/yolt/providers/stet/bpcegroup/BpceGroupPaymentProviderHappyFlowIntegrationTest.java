package com.yolt.providers.stet.bpcegroup;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/bpce/pis", httpsPort = 0, port = 0)
@ActiveProfiles("bpce")
@Import(BpceGroupTestConfig.class)
class BpceGroupPaymentProviderHappyFlowIntegrationTest {

    private final BpceGroupSampleAuthenticationMeans sampleAuthenticationMeans = new BpceGroupSampleAuthenticationMeans();
    
    @Autowired
    private Signer signer;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("BanquePopulairePaymentProviderV2")
    private GenericPaymentProviderV3 banquePopulairePaymentProvider;


    private Stream<SepaPaymentProvider> getPaymentProviders() {
        return Stream.of(banquePopulairePaymentProvider);
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldInitiatePayment(GenericPaymentProviderV3 paymentProvider) {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7613807008043001965406128"))
                .creditorName("myMerchant")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7610907000301234567890125"))
                .endToEndIdentification("b678bef5-dd48-4df1-81d2-1b14e5ca4e01")
                .executionDate(LocalDate.of(2020, 1, 1))
                .instructedAmount(new SepaAmountDTO(new BigDecimal("10.51")))
                .remittanceInformationUnstructured("MyRemittanceInformation123")
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO).setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .setState("1")
                .build();

        // when
        LoginUrlAndStateDTO result = paymentProvider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("https://bpce.com/psuId?paymentRequestRessourceId=0000000180-1551358254000131359238543&nonce=Id-2ed9775ce61639e9a3c94ecc");
        assertThat(result.getProviderState()).isEqualTo("{\"paymentId\":\"0000000180-1551358254000131359238543\"}");
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
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState("{\"paymentId\":\"0000000180-1551358254000131359238543\"}")
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.1.0.0")
                .build();

        //when
        SepaPaymentStatusResponseDTO result = paymentProvider.submitPayment(submitPaymentRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo("0000000180-1551358254000131359238543");
        assertThat(result.getProviderState()).isEqualTo("{\"paymentId\":\"0000000180-1551358254000131359238543\"}");
        assertThat(result.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.PDNG.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                });
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    void shouldGetPaymentStatus(GenericPaymentProviderV3 paymentProvider) {

        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId("0000000180-1551358254000131359238543")
                .setProviderState(null)
                .setSigner(signer)
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.1.0.0")
                .build();

        // when
        SepaPaymentStatusResponseDTO responseDTO = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo("0000000180-1551358254000131359238543");
        assertThat(responseDTO.getProviderState()).isEqualTo("{\"paymentId\":\"0000000180-1551358254000131359238543\"}");
        assertThat(responseDTO.getPaymentExecutionContextMetadata())
                .extracting(PaymentExecutionContextMetadata::getPaymentStatuses)
                .satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(StetPaymentStatus.PDNG.toString());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                });
    }
}