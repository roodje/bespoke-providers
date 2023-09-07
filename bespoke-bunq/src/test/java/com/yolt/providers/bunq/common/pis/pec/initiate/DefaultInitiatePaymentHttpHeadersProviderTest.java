package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentRequest;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentHttpHeadersProviderTest {

    @Mock
    BunqHttpHeaderProducer httpHeaderProducer;
    @Mock
    BunqProperties bunqProperties;

    private DefaultInitiatePaymentHttpHeadersProvider paymentHeadersProvider;

    @BeforeEach
    void setUp() {
        paymentHeadersProvider = new DefaultInitiatePaymentHttpHeadersProvider(new DefaultEndpointUrlProvider(bunqProperties), httpHeaderProducer);
    }

    @Test
    void shouldReturnHttpHeadersWhenCorrectDataAreProvided() throws JsonProcessingException {
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(AuthMeans.prepareAuthMeansV2(), "BUNQ");
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String sessionToken = "someSessionToken";
        PaymentServiceProviderDraftPaymentRequest paymentRequestBody = mock(PaymentServiceProviderDraftPaymentRequest.class);
        DefaultInitiatePaymentPreExecutionResult preExecutionResult = new DefaultInitiatePaymentPreExecutionResult(
                null, null, "http://yolt.com/callback", "someState",
                authMeans.getClientId(), authMeans.getPsd2UserId(), sessionToken, 1L, keyPair);
        when(bunqProperties.getBaseUrl()).thenReturn("https://baseurl.com");
        HttpHeaders expectedHeaders = mock(HttpHeaders.class);
        when(httpHeaderProducer.getSignedHeaders(keyPair, sessionToken, paymentRequestBody, "https://baseurl.com/user/28196/payment-service-provider-draft-payment"))
                .thenReturn(expectedHeaders);

        //when
        HttpHeaders result = paymentHeadersProvider.provideHttpHeaders(preExecutionResult, paymentRequestBody);

        //then
        assertThat(result).isEqualTo(expectedHeaders);
        verifyNoInteractions(expectedHeaders);
    }
}