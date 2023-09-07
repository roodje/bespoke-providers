package com.yolt.providers.rabobank;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.rabobank.config.RabobankProperties;
import lombok.experimental.UtilityClass;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static com.yolt.providers.rabobank.RabobankObjectMapperV3.OBJECT_MAPPER;

@UtilityClass
@Deprecated //Use RabobankAisHttpClientFactory instead see C4PO-8788 for more details
public class RestTemplateSupplierV3 {

    public static RestTemplate getRestTemplate(final RestTemplateManager restTemplateManager,
                                               final RabobankAuthenticationMeans authenticationMeans,
                                               final RabobankProperties properties) {
        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder.mutualTlsBuilder(authenticationMeans.getTransportKid(),
                authenticationMeans.getClientCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .messageConverters(new MappingJackson2HttpMessageConverter(OBJECT_MAPPER), new FormHttpMessageConverter())
                        .build())
                .defaultKeepAliveTimeoutInMillis(10000L)
                .build()
        );
    }
}
