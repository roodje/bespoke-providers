package com.yolt.providers.volksbank.common.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.volksbank.FakeRestTemplateManager;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test suite contains all happy flows for Volksbank group PIS provider.
 * Tests are parametrized and run for all {@link VolksbankSepaPaymentProviderV3} providers in group.
 * <p>
 * Covered flows:
 * - initiating payment process
 * - submitting payment process (which involves getting for payment status)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("volksbank")
@AutoConfigureWireMock(httpsPort = 0, port = 0, stubs = "classpath:/stubs/volksbank/api_1.1/pis/happy_flow")
public class VolksbankSepaPaymentProviderV3HappyFlowIntegrationTest {

    @Autowired
    @Qualifier("ASNBankSepaPaymentProviderV3")
    private VolksbankSepaPaymentProviderV3 asnProviderV3;

    @Autowired
    @Qualifier("SNSBankSepaPaymentProviderV3")
    private VolksbankSepaPaymentProviderV3 snsProviderV3;

    @Autowired
    @Qualifier("RegioBankSepaPaymentProviderV3")
    private VolksbankSepaPaymentProviderV3 regioProviderV3;

    Stream<SepaPaymentProvider> getVolksbankProviders() {
        return Stream.of(regioProviderV3, snsProviderV3, asnProviderV3);
    }

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldReturnLoginUrlAndStateResponseWithProperLoginAndStateForInitiatePaymentWithCorrectData(SepaPaymentProvider paymentProviderUnderTest) {
        // given
        var sepaInitiatePaymentRequestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("NL64MAART0948305290")
                        .build())
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("NL55WIND0000012345")
                        .build())
                .creditorName("John Doe")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("123.50"))
                        .build())
                .remittanceInformationUnstructured("payment for 11 currant buns")
                .build();
        var initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setState("state")
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setRequestDTO(sepaInitiatePaymentRequestDTO)
                .build();

        // when
        var result = paymentProviderUnderTest.initiatePayment(initiatePaymentRequest);

        // then
        var uriComponents = UriComponentsBuilder.fromHttpUrl(result.getLoginUrl()).build();
        assertThat(uriComponents.getQueryParams())
                .containsEntry("response_type", Collections.singletonList("code"))
                .containsEntry("scope", Collections.singletonList("PIS"))
                .containsEntry("paymentId", Collections.singletonList("SNS0123456789012"))
                .containsEntry("redirect_uri", Collections.singletonList(initiatePaymentRequest.getBaseClientRedirectUrl()))
                .containsEntry("client_id", Collections.singletonList(authenticationMeans.get(VolksbankAuthenticationMeans.CLIENT_ID_NAME).getValue()))
                .containsEntry("state", Collections.singletonList(initiatePaymentRequest.getState()));
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"SNS0123456789012"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RCVD");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }));
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldReturnSepaPaymentStatusResponseWithProperPaymentIdAndStatusAcceptedForSubmitPaymentWithCorrectData(SepaPaymentProvider paymentProviderUnderTest) {
        // given
        var submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?code=123456789&state=state")
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("""
                        {"paymentId":"SNS0123456789012"}""")
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .build();

        // when
        var result = paymentProviderUnderTest.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("SNS0123456789012");
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"SNS0123456789012"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACCC");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldReturnSepaPaymentStatusResponseWithPaymentIdAndStatusForGetStatusWhenPaymentIdIsProvidedInRequest(SepaPaymentProvider paymentProviderUnderTest) {
        // given
        var getStatusRequest = createGetStatusRequest(true);

        // when
        var result = paymentProviderUnderTest.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("SNS0123456789012");
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"SNS0123456789012"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACCC");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldReturnSepaPaymentStatusResponseWithPaymentIdAndStatusForGetStatusWhenPaymentIdIsNotProvidedInRequest(SepaPaymentProvider paymentProviderUnderTest) {
        // given
        var getStatusRequest = createGetStatusRequest(false);

        // when
        var result = paymentProviderUnderTest.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("SNS0123456789012");
        assertThat(result.getProviderState()).isEqualTo("""
                {"paymentId":"SNS0123456789012"}""");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACCC");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.COMPLETED);
                }));
    }

    private GetStatusRequest createGetStatusRequest(boolean withPaymentId) {
        return new GetStatusRequestBuilder()
                .setPaymentId(withPaymentId ? "SNS0123456789012" : null)
                .setProviderState(withPaymentId ? null : """
                        {"paymentId":"SNS0123456789012"}""")
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();
    }
}