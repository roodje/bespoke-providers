package com.yolt.providers.openbanking.ais.amexgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.client.ClientHttpRequestInterceptor;

public class AmexGroupHttpClientFactory extends DefaultHttpClientFactory {

    public AmexGroupHttpClientFactory(DefaultProperties properties, MeterRegistry registry, ObjectMapper mapper) {
        super(properties, registry, mapper);
    }

    @Override
    protected ClientHttpRequestInterceptor[] getClientHttpRequestInterceptors() {
        return new ClientHttpRequestInterceptor[]{
                new AmexContentTypeHeaderInterceptor()
        };
    }

}
