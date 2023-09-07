package com.yolt.providers.stet.labanquepostale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import com.yolt.providers.stet.generic.GenericPaymentProviderV2;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/labanquepostale/pis/unsuccessful-redirect/", httpsPort = 0, port = 0)
@ActiveProfiles("labanquepostale")
class LaBanquePostaleGroupPaymentProviderUnsuccessfulRedirectIntegrationTest {

    private static final String PAYMENT_ID = "98ff0a93-7f37-41a9-8563-214f898c7b1c";

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("LaBanquePostalePaymentProviderV2")
    private GenericPaymentProviderV3 laBanquePostalePaymentProvider;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Stream<GenericPaymentProviderV3> getLaBanquePostalePaymentProviders() {
        return Stream.of(laBanquePostalePaymentProvider);
    }

    @ParameterizedTest
    @MethodSource("getLaBanquePostalePaymentProviders")
    void shouldThrowConfirmationFailedExceptionWhenPsuAuthenticationFactorIsMissing(GenericPaymentProviderV3 sepaPaymentProvider) {
        // given
        String jsonProviderState = LaBanquePostaleGroupSampleMeans.createPaymentJsonProviderState(objectMapper, PAYMENT_ID);

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setProviderState(jsonProviderState)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback-acc/payment?state=TEST_STATE")
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> sepaPaymentProvider.submitPayment(submitPaymentRequest);
        AssertionsForClassTypes.assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(PaymentExecutionTechnicalException.class)
                .hasMessage("submit_preparation_error")
                .hasCauseExactlyInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("PSU Authentication Factor value is missing");
    }

    @ParameterizedTest
    @MethodSource("getLaBanquePostalePaymentProviders")
    void shouldThrowConfirmationFailedExceptionWhenErrorQueryParamIsPresentInRedirect(GenericPaymentProviderV3 sepaPaymentProvider) {
        // given
        String jsonProviderState = LaBanquePostaleGroupSampleMeans.createPaymentJsonProviderState(objectMapper, PAYMENT_ID);

        String redirectUrlPostedBackFromSite = "https://www.yolt.com/callback-acc/payment?state=TEST_STATE&error=true&psuAuthenticationFactor=JJKJKJ788GKJKJBK";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setProviderState(jsonProviderState)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite(redirectUrlPostedBackFromSite)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> sepaPaymentProvider.submitPayment(submitPaymentRequest);
        // then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(PaymentExecutionTechnicalException.class)
                .hasMessage("submit_preparation_error")
                .hasCauseExactlyInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("Got error in callback URL. Payment confirmation failed. Redirect url: " + redirectUrlPostedBackFromSite);
    }
}