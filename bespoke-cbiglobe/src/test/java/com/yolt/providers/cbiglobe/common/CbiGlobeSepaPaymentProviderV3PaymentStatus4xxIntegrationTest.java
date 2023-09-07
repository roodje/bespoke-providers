package com.yolt.providers.cbiglobe.common;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.SignerMock;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.GetStatusRequestBuilder;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains flow for scenarios when 4xx error occurs during payment submission process and getting payment status
 * In such case we assume payment is not properly confirmed and we set UNKNOWN payment status for payment submission
 * and throw {@link PaymentExecutionTechnicalException} for getting payment status
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(
        stubs = "classpath:/stubs/pis/3.0/payment_status_4xx",
        httpsPort = 0, port = 0)
@ActiveProfiles("cbiglobe")
public class CbiGlobeSepaPaymentProviderV3PaymentStatus4xxIntegrationTest {

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Signer signer = new SignerMock();

    @Autowired
    @Qualifier("PosteItalianeSepaPaymentProviderV3")
    private CbiGlobeSepaPaymentProviderV3 posteItalianePaymentProviderV3;

    @Autowired
    @Qualifier("IntesaSanpaoloSepaPaymentProviderV3")
    private CbiGlobeSepaPaymentProviderV3 intesaSanpaoloPaymentProviderV3;

    private Stream<SepaPaymentProvider> getPaymentProviders() {
        return Stream.of(posteItalianePaymentProviderV3, intesaSanpaoloPaymentProviderV3);
    }

    @BeforeEach
    void initialize() {
        authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnResponseWithEmptyPaymentIdAndProperStatusesInPecMetadataForSubmitPaymentWhen4xxError(SepaPaymentProvider paymentProvider) {
        // given
        var submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?code=123456789&state=state") //not know what will be the int the url
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("""
                        {"paymentId":"SOME-PROVIDER-STATE"}""")
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .setSigner(signer)
                .build();

        // when
        var result = paymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isNotEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RJCT");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionForPaymentStatusWhen4xxError(SepaPaymentProvider paymentProvider) {
        // given
        var getStatusRequest = new GetStatusRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("""
                        {"paymentId":"SOME-PROVIDER-STATE"}""")
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .setSigner(signer)
                .build();

        // when-then
        var result = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isNotEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RJCT");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }
}
