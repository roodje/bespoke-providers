package com.yolt.providers.stet.lclgroup.lcl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.lclgroup.LclGroupTestConfig;
import com.yolt.providers.stet.lclgroup.lcl.configuration.LclStetProperties;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = LclGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lclgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/lcl/pis/happy-flow", httpsPort = 0, port = 0)
class LclPaymentProviderHappyFlowIntegrationTest {
    private static final String STATE = "state";
    private static final String CLIENT_ID = "client-id";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String CALLBACK_URL = "https://www.yolt.com/callback/payment";
    private static final String PAYMENT_ID = "123";
    private static final String SERIALIZED_PAYMENT_ID = "{\"paymentId\":\"" + PAYMENT_ID + "\"}";

    private static Map<String, BasicAuthenticationMean> authenticationMeans = new LclGroupSampleAuthenticationMeans().getSampleAuthMeans();

    @Autowired
    @Qualifier("LclPaymentProviderV2")
    private GenericPaymentProviderV3 lclPaymentProviderV2;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("LclStetProperties")
    private LclStetProperties lclProperties;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signer;

    private Stream<GenericPaymentProviderV3> getLclProviders() {
        return Stream.of(lclPaymentProviderV2);
    }

    @ParameterizedTest
    @MethodSource("getLclProviders")
    void shouldInitiatePayment(GenericPaymentProviderV3 provider) {
        InitiatePaymentRequest initiatePaymentRequest = getInitiatePaymentRequest(authenticationMeans);

        // when
        LoginUrlAndStateDTO result = provider.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getLoginUrl()).isEqualTo("https://psd2.aspsp/consent-approval?client_id=" + CLIENT_ID + "&redirect_uri=" + CALLBACK_URL + "/?state=state");
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
    @MethodSource("getLclProviders")
    void shouldSubmitPayment(GenericPaymentProviderV3 provider) throws JsonProcessingException {
        // given
        PaymentProviderState providerState = PaymentProviderState.builder()
                .paymentId(PAYMENT_ID)
                .build();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setProviderState(objectMapper.writeValueAsString(providerState))
                .setAuthenticationMeans(authenticationMeans)
                .setRedirectUrlPostedBackFromSite(CALLBACK_URL)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        //when
        SepaPaymentStatusResponseDTO result = provider.submitPayment(submitPaymentRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo(PAYMENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getLclProviders")
    void shouldGetPaymentStatus(GenericPaymentProviderV3 provider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequest(null,
                PAYMENT_ID,
                authenticationMeans,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()));

        //when
        SepaPaymentStatusResponseDTO result = provider.getStatus(getStatusRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo(PAYMENT_ID);
    }

    private InitiatePaymentRequest getInitiatePaymentRequest(Map<String, BasicAuthenticationMean> authMeans) {
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7613807008043001965406128"))
                .creditorName("myMerchant")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "FR7613807008043001965405255"))
                .endToEndIdentification("MyEndToEndId123")
                .executionDate(LocalDate.of(2020, 1, 1))
                .instructedAmount(new SepaAmountDTO(new BigDecimal("10.01")))
                .remittanceInformationUnstructured("MyRemittanceInformation123")
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO).setBaseClientRedirectUrl(CALLBACK_URL)
                .setAuthenticationMeans(authMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setState(STATE)
                .build();
        return initiatePaymentRequest;
    }
}
