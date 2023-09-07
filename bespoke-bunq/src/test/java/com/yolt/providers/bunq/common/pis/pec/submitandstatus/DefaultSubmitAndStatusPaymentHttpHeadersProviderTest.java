package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
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
class DefaultSubmitAndStatusPaymentHttpHeadersProviderTest {

    @Mock
    BunqHttpHeaderProducer httpHeaderProducer;
    @Mock
    BunqProperties bunqProperties;

    private DefaultSubmitAndStatusPaymentHttpHeadersProvider paymentHeadersProvider;

    @BeforeEach
    void setUp() {
        paymentHeadersProvider = new DefaultSubmitAndStatusPaymentHttpHeadersProvider(new DefaultEndpointUrlProvider(bunqProperties), httpHeaderProducer);
    }

    @Test
    void shouldReturnHttpHeadersWhenCorrectDataAreProvided() throws JsonProcessingException {
        //given
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(AuthMeans.prepareAuthMeansV2(), "BUNQ");
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String sessionToken = "someSessionToken";
        DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult = new DefaultSubmitAndStatusPaymentPreExecutionResult(
                null, 123, authMeans.getPsd2UserId(), sessionToken, 1L, keyPair);
        when(bunqProperties.getBaseUrl()).thenReturn("https://baseurl.com");
        HttpHeaders expectedHeaders = mock(HttpHeaders.class);
        when(httpHeaderProducer.getSignedHeaders(keyPair, sessionToken, new byte[]{}, "https://baseurl.com/user/28196/payment-service-provider-draft-payment/123"))
                .thenReturn(expectedHeaders);

        //when
        HttpHeaders result = paymentHeadersProvider.provideHttpHeaders(preExecutionResult, null);

        //then
        assertThat(result).isEqualTo(expectedHeaders);
        verifyNoInteractions(expectedHeaders);
    }
}