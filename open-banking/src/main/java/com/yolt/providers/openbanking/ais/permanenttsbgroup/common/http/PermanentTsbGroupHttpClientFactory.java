package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.properties.PermanentTsbGroupProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.client.ClientHttpRequestInterceptor;

public class PermanentTsbGroupHttpClientFactory extends DefaultHttpClientFactory {

    private final PermanentTsbGroupProperties properties;

    public PermanentTsbGroupHttpClientFactory(final PermanentTsbGroupProperties properties,
                                              final MeterRegistry registry,
                                              final ObjectMapper mapper) {
        super(properties, registry, mapper);
        this.properties = properties;
    }

    @Override
    protected ClientHttpRequestInterceptor[] getClientHttpRequestInterceptors() {
        return new ClientHttpRequestInterceptor[]{
                new PermanentTsbGroupContentTypeHeaderInterceptor(properties.getRegistrationUrl())
        };
    }
}
