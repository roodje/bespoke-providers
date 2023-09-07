package com.yolt.providers.stet.labanquepostale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import com.yolt.providers.stet.generic.GenericPaymentProviderV2;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.config.LaBanquePostaleProperties;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus.EXECUTION_FAILED;
import static com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus.INITIATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/labanquepostale/pis/confirm-payment-403", httpsPort = 0, port = 0)
@ActiveProfiles("labanquepostale")
class LaBanquePostaleGroupPaymentProviderConfirmPaymentHttp403IntegrationTest {

    private static final String PAYMENT_ID = "98ff0a93-7f37-41a9-8563-214f898c7b1c";

    @Autowired
    private Signer signer;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("LaBanquePostaleStetProperties")
    private LaBanquePostaleProperties laBanquePostaleProperties;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("LaBanquePostalePaymentProviderV2")
    private GenericPaymentProviderV3 laBanquePostalePaymentProvider;

    private Stream<Arguments> getDataProviders() {
        return Stream.of(Arguments.of(laBanquePostalePaymentProvider, laBanquePostaleProperties));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowConfirmationFailedExceptionWhenHttp403IsReceived(GenericPaymentProviderV3 paymentProvider) {
        // given
        String jsonProviderState = LaBanquePostaleGroupSampleMeans.createPaymentJsonProviderState(objectMapper, PAYMENT_ID);

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setProviderState(jsonProviderState)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback-acc/payment?state=TEST_STATE&psuAuthenticationFactor=403")
                .build();

        // when
        SepaPaymentStatusResponseDTO result = paymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getProviderState()).isNotEmpty();
        assertThat(result.getPaymentExecutionContextMetadata())
                .satisfies(pecMetadata -> assertThat(pecMetadata.getPaymentStatuses())
                        .extracting(
                                statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("Forbidden", "Something went wrong", EXECUTION_FAILED));
    }
}