package com.yolt.providers.bunq.common.pis;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.RestTemplateManagerMock;
import com.yolt.providers.bunq.TestApp;
import com.yolt.providers.bunq.common.BunqPaymentProvider;
import com.yolt.providers.bunq.common.pis.pec.PaymentProviderState;
import com.yolt.providers.bunq.common.pis.pec.exception.ProviderStateSerializationException;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/pis/happy-flow", httpsPort = 0, port = 0)
class BunqPaymentProviderHappyFlowIntegrationTest {

    private static final String SESSION_TOKEN = "a3cc2d56b7d30dae471c418b16cd15d674aa06ffa9f45beae39dbb9a8ac749c9";
    private static final Map<String, BasicAuthenticationMean> AUTHENTICATION_MEANS = AuthMeans.prepareAuthMeansV2();

    @Autowired
    private BunqPaymentProvider bunqPaymentProvider;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private ObjectMapper objectMapper;
    private RestTemplateManagerMock restTemplateManagerMock;

    @BeforeEach
    public void beforeEach() {
        objectMapper = new ObjectMapper();
        restTemplateManagerMock = new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
    }

    @Mock
    private Signer signer;

    @Test
    void shouldInitiatePayment() {
        // given
        InitiatePaymentRequest initiatePaymentRequest = createInitiatePaymentRequest();

        //when
        LoginUrlAndStateDTO result = bunqPaymentProvider.initiatePayment(initiatePaymentRequest);

        //then
        assertThat(result.getLoginUrl()).isEqualTo("https://oauth.bunq.com/auth?response_type=code&client_id=aabb&redirect_uri=https://www.yolt.com/callback/payment&state=someState");
        assertThat(result.getProviderState())
                .contains("\"paymentId\":12345")
                .contains("\"paymentType\":\"SINGLE\"")
                .contains("\"sessionToken\":\"a3cc2d56b7d30dae471c418b16cd15d674aa06ffa9f45beae39dbb9a8ac749c9\"")
                .contains("\"keyPairPublic\"")
                .contains("\"expirationTime\"")
                .contains("\"keyPairPrivate\"");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("INITIATED");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @Test
    void shouldSubmitPayment() {
        // given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var keyPair = SecurityUtils.generateKeyPair();
        var providerState = new PaymentProviderState(12345, PaymentType.SINGLE, SESSION_TOKEN, Instant.now().minusSeconds(100L).toEpochMilli(), SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair));
        var submitPaymentRequest = createSubmitPaymentRequest(authMeans, providerState);

        //when
        SepaPaymentStatusResponseDTO result = bunqPaymentProvider.submitPayment(submitPaymentRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo("12345");
        assertThat(result.getProviderState())
                .contains("\"paymentId\":12345")
                .contains("\"paymentType\":\"SINGLE\"")
                .contains("\"sessionToken\":\"a3cc2d56b7d30dae471c418b16cd15d674aa06ffa9f45beae39dbb9a8ac749c9\"")
                .contains("\"keyPairPublic\"")
                .contains("\"expirationTime\"")
                .contains("\"keyPairPrivate\"");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("COMPLETED", "", EnhancedPaymentStatus.COMPLETED));
    }

    @Test
    void shouldGetPaymentStatus() {
        // given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var keyPair = SecurityUtils.generateKeyPair();
        var providerState = new PaymentProviderState(12345, PaymentType.SINGLE, SESSION_TOKEN, Instant.now().minusSeconds(100L).toEpochMilli(), SecurityUtils.getPublicKeyFormattedString(keyPair), SecurityUtils.getPrivateKeyFormattedString(keyPair));
        var getPaymentStatusRequest = createGetPaymentStatusRequest(authMeans, providerState);

        //when
        SepaPaymentStatusResponseDTO result = bunqPaymentProvider.getStatus(getPaymentStatusRequest);

        //then
        assertThat(result.getPaymentId()).isEqualTo("12345");
        assertThat(result.getProviderState())
                .contains("\"paymentId\":12345")
                .contains("\"paymentType\":\"SINGLE\"")
                .contains("\"sessionToken\":\"a3cc2d56b7d30dae471c418b16cd15d674aa06ffa9f45beae39dbb9a8ac749c9\"")
                .contains("\"keyPairPublic\"")
                .contains("\"expirationTime\"")
                .contains("\"keyPairPrivate\"");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("COMPLETED", "", EnhancedPaymentStatus.COMPLETED));
    }

    private String serializeProviderState(PaymentProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot serialize provider state", e);
        }
    }

    private InitiatePaymentRequest createInitiatePaymentRequest() {
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164300"))
                .creditorName("Some Name")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164322"))
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("5877.78")))
                .remittanceInformationUnstructured("unstructured-information")
                .build();
        return new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setState("someState")
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("111.1.111.111")
                .build();
    }

    private SubmitPaymentRequest createSubmitPaymentRequest(Map<String, BasicAuthenticationMean> authMeans,
                                                            PaymentProviderState providerState) {
        return new SubmitPaymentRequestBuilder()
                .setProviderState(serializeProviderState(providerState))
                .setAuthenticationMeans(authMeans)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback?code=someCode&state=randomState")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("fakePsuIpAddress")
                .build();
    }

    private GetStatusRequest createGetPaymentStatusRequest(Map<String, BasicAuthenticationMean> authMeans,
                                                           PaymentProviderState providerState) {
        return new GetStatusRequestBuilder()
                .setProviderState(serializeProviderState(providerState))
                .setPaymentId("12345")
                .setAuthenticationMeans(authMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("fakePsuIpAddress")
                .build();
    }
}

