package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqPisHttpClient;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitAndStatusPaymentHttpRequestInvokerTest {

    @Mock
    BunqProperties propeties;
    @Mock
    BunqPisHttpClient httpClient;

    private DefaultSubmitAndStatusPaymentHttpRequestInvoker requestInvoker;

    @BeforeEach
    void setUp() {
        requestInvoker = new DefaultSubmitAndStatusPaymentHttpRequestInvoker(new DefaultEndpointUrlProvider(propeties));
    }

    @Test
    void shouldReturnJsonNodeAsResponseWhenCorrectDataIsProvided() throws TokenInvalidException {
        //given
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(AuthMeans.prepareAuthMeansV2(), "BUNQ");
        when(propeties.getBaseUrl()).thenReturn("https://bunq.api.com");
        DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult = new DefaultSubmitAndStatusPaymentPreExecutionResult(httpClient, 123, authMeans.getPsd2UserId(), null, 1L, null);
        HttpEntity mockedHttpEntity = mock(HttpEntity.class);
        ResponseEntity mockedResponseEntity = mock(ResponseEntity.class);
        when(httpClient.getPaymentStatus(mockedHttpEntity, "https://bunq.api.com/user/28196/payment-service-provider-draft-payment/123")).thenReturn(mockedResponseEntity);

        //given
        ResponseEntity result = requestInvoker.invokeRequest(mockedHttpEntity, preExecutionResult);

        //then
        assertThat(result).isEqualTo(mockedResponseEntity);
    }
}