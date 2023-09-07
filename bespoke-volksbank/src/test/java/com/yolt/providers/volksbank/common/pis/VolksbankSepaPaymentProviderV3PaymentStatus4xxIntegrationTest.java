package com.yolt.providers.volksbank.common.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.volksbank.FakeRestTemplateManager;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
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
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains flow for scenarios when 4xx error
 * occurs during payment submission process - while getting payment status
 * In such case we assume payment is not properly confirmed and we throw {@link ConfirmationFailedException}.
 * Tests are parametrized and run for all {@link VolksbankSepaPaymentProviderV3} providers in group.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("volksbank")
@AutoConfigureWireMock(httpsPort = 0, port = 0, stubs = {"classpath:/stubs/volksbank/api_1.1/pis/payment_status_4xx"})
public class VolksbankSepaPaymentProviderV3PaymentStatus4xxIntegrationTest {

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
    public void shouldThrowConfirmationFailedExceptionForSubmitPaymentWhen4xxErrorWhilePaymentStatusRequest(SepaPaymentProvider paymentProviderUnderTest) {
        // given
        var submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?code=123456789&state=state")
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("""
                        {"paymentId":"SNS0011223344"}""")
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .build();

        // when
        var result = paymentProviderUnderTest.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata -> assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                .contains("RESOURCE_UNKNOWN",
                        "The payment could not be found.",
                        EnhancedPaymentStatus.UNKNOWN));
    }
}
