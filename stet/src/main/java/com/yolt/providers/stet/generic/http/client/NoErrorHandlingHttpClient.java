package com.yolt.providers.stet.generic.http.client;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

//TODO: C4PO-7223 Remove this class and migrate to new solution, when ticket will be completed
@Deprecated
public class NoErrorHandlingHttpClient extends DefaultHttpClient implements HttpErrorHandler {

    public NoErrorHandlingHttpClient(MeterRegistry registry,
                                     RestTemplate restTemplate,
                                     String provider) {
        super(registry, restTemplate, provider);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String endpoint,
                                          HttpMethod method,
                                          HttpEntity body,
                                          String prometheusPathOverride,
                                          Class<T> responseType,
                                          HttpErrorHandler errorHandler,
                                          String... uriArgs) throws TokenInvalidException {
        return super.exchange(endpoint, method, body, prometheusPathOverride, responseType, this, uriArgs);
    }

    @Override
    public <T> T exchangeForBody(String endpoint,
                                 HttpMethod method,
                                 HttpEntity body,
                                 String prometheusPathOverride,
                                 Class<T> responseType,
                                 String... uriArgs) throws TokenInvalidException {
        return this.exchange(endpoint, method, body, prometheusPathOverride, responseType, this, uriArgs).getBody();
    }

    @Override
    public <T> ResponseEntity<T> exchange(String endpoint,
                                          HttpMethod method,
                                          HttpEntity body,
                                          String prometheusPathOverride,
                                          Class<T> responseType,
                                          String... uriArgs) throws TokenInvalidException {
        return this.exchange(endpoint, method, body, prometheusPathOverride, responseType, this, uriArgs);
    }

    @Override
    public void handle(HttpStatusCodeException e) {
        throw e;
    }
}
