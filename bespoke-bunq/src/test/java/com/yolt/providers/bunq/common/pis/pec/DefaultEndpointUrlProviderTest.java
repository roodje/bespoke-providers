package com.yolt.providers.bunq.common.pis.pec;

import com.yolt.providers.bunq.common.configuration.BunqProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultEndpointUrlProviderTest {

    @Mock
    BunqProperties bunqProperties;

    private DefaultEndpointUrlProvider endpointProvider;

    @BeforeEach
    void setUp() {
        endpointProvider = new DefaultEndpointUrlProvider(bunqProperties);
    }

    @Test
    void shouldReturnUrlForInitiateDraftPaymentWithUserId() {
        //given
        when(bunqProperties.getBaseUrl()).thenReturn("https://baseurl.com");

        //when
        String result = endpointProvider.getInitiateDraftPaymentUrl(12345L);

        //then
        assertThat(result).isEqualTo("https://baseurl.com/user/12345/payment-service-provider-draft-payment");
    }

    @Test
    void shouldReturnUrlForInitiateSession() {
        //given
        when(bunqProperties.getBaseUrl()).thenReturn("https://baseurl.com");

        //when
        String result = endpointProvider.getSessionServerUrl();

        //then
        assertThat(result).isEqualTo("https://baseurl.com/session-server");
    }
}