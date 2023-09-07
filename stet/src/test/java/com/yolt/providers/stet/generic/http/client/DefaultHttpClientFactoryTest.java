package com.yolt.providers.stet.generic.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Deprecated
class DefaultHttpClientFactoryTest {

    private static final UUID CLIENT_TRANSPORT_KEY_ID = UUID.fromString("1c8b1faa-ca63-4582-853a-816b28df2fd8");
    private static final X509Certificate CLIENT_TRANSPORT_CERTIFICATE = Mockito.mock(X509Certificate.class);

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DefaultAuthenticationMeans authMeans;

    @InjectMocks
    private DefaultHttpClientFactory httpClientFactory;

    @Mock
    private ObjectReader objectReader;

    @Test
    void shouldReturnSpecificHttpMessageConverters() {
        // given
        when(objectMapper.reader()).thenReturn(objectReader);

        // when
        HttpMessageConverter[] httpMessageConverters = httpClientFactory.getHttpMessageConverters();

        // then
        HashSet<HttpMessageConverter> uniqueHttpMessageConverters = new HashSet<>(List.of(httpMessageConverters));
        assertThat(uniqueHttpMessageConverters).hasSize(4);
    }

    @Test
    void shouldReturnConfiguredDefaultUriBuilderFactory() {
        // given-when
        DefaultUriBuilderFactory uriBuilderFactory = httpClientFactory.configureDefaultUriBuilderFactory();

        // then
        assertThat(uriBuilderFactory.getEncodingMode()).isEqualTo(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        assertThat(uriBuilderFactory.shouldParsePath()).isEqualTo(false);
    }

    @Test
    void shouldReturnConfiguredRestTemplate() {
        // given
        String baseUrl = "https://example.com";
        RestTemplate expectedRestTemplate = new RestTemplate();

        when(authMeans.getClientTransportKeyId())
                .thenReturn(CLIENT_TRANSPORT_KEY_ID);
        when(authMeans.getClientTransportCertificate())
                .thenReturn(CLIENT_TRANSPORT_CERTIFICATE);
        ArgumentMatcher<RestTemplateManagerConfiguration> argumentMatcher = c -> c.getPrivateTransportKid().equals(CLIENT_TRANSPORT_KEY_ID) && c.getClientCertificateChain()[0].equals(CLIENT_TRANSPORT_CERTIFICATE);
        when(restTemplateManager.manage(argThat(argumentMatcher)))
                .thenReturn(expectedRestTemplate);

        // when
        RestTemplate restTemplate = httpClientFactory.createRestTemplate(restTemplateManager, authMeans, baseUrl);

        // then
        assertThat(restTemplate).isEqualTo(expectedRestTemplate);
        verify(restTemplateManager).manage(argThat(argumentMatcher));
    }

    @Test
    void shouldReturnConfiguredHttpClient() {
        // given
        String baseUrl = "https://example.com";
        String providerName = "provider name";
        RestTemplate expectedRestTemplate = new RestTemplate();

        when(authMeans.getClientTransportKeyId())
                .thenReturn(CLIENT_TRANSPORT_KEY_ID);
        when(authMeans.getClientTransportCertificate())
                .thenReturn(CLIENT_TRANSPORT_CERTIFICATE);
        ArgumentMatcher<RestTemplateManagerConfiguration> argumentMatcher = c -> c.getPrivateTransportKid().equals(CLIENT_TRANSPORT_KEY_ID) && c.getClientCertificateChain()[0].equals(CLIENT_TRANSPORT_CERTIFICATE);
        when(restTemplateManager.manage(argThat(argumentMatcher))).thenReturn(expectedRestTemplate);

        // when
        HttpClient httpClient = httpClientFactory.createHttpClient(restTemplateManager, authMeans, baseUrl, providerName);

        // then
        assertThat(httpClient).isInstanceOf(NoErrorHandlingHttpClient.class);
        verify(restTemplateManager).manage(argThat(argumentMatcher));
    }
}
