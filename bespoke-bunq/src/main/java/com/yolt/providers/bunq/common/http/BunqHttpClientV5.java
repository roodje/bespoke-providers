package com.yolt.providers.bunq.common.http;

import com.bunq.sdk.http.BunqRequestBody;
import com.bunq.sdk.http.BunqRequestBuilder;
import com.bunq.sdk.http.ContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.common.exception.BunqPostBodyMalformedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Optional;

import static com.yolt.providers.bunq.common.http.BunqHttpErrorHandler.BUNQ_HTTP_ERROR_HANDLER;

public class BunqHttpClientV5 extends DefaultHttpClient {

    private final BunqHttpHeaderProducer headerProducer;
    private final ObjectMapper objectMapper;

    public BunqHttpClientV5(MeterRegistry registry, RestTemplate restTemplate, String provider, BunqHttpHeaderProducer headerProducer, ObjectMapper objectMapper) {
        super(registry, restTemplate, provider);
        this.headerProducer = headerProducer;
        this.objectMapper = objectMapper;
    }

    <T> ResponseEntity<T> get(final String fullPath,
                              final KeyPair keyPair,
                              final String clientAuthentication,
                              final Class<T> responseType,
                              final String prometheusMater) throws TokenInvalidException {
        BunqRequestBuilder requestBuilder = new BunqRequestBuilder().get();
        requestBuilder.url(fullPath);

        HttpHeaders headers = headerProducer.getSignedHeaders(keyPair, clientAuthentication, requestBuilder);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        return exchange(fullPath, HttpMethod.GET, requestEntity, prometheusMater, responseType);
    }

    <T> ResponseEntity<T> postEmptyBody(final String fullPath,
                                        final Class<T> responseType,
                                        final String prometheusMeter) throws TokenInvalidException {
        return exchange(fullPath, HttpMethod.POST, new HttpEntity<>(null), prometheusMeter, responseType);
    }

    // This method is ONLY used for the initial connection call to com.yolt.providers.bunq because that call MUST be unsigned.
    <T> ResponseEntity<T> postUnsignedRequest(final String fullPath,
                                              final Object data,
                                              final Class<T> responseType,
                                              final String prometheusMater) throws TokenInvalidException {
        String content = Optional.ofNullable(data).map(this::mapObjectToJsonString).orElse("");
        HttpHeaders headers = headerProducer.getMandatoryHttpHeaders();
        HttpEntity<Object> requestEntity = new HttpEntity<>(content, headers);
        return exchange(fullPath, HttpMethod.POST, requestEntity, prometheusMater, responseType);
    }

    <T> ResponseEntity<T> buildSignedPostRequest(final String fullPath,
                                                 final KeyPair keyPair,
                                                 final String clientAuthentication,
                                                 final Object data,
                                                 final Class<T> responseType,
                                                 final String prometheusMater) throws TokenInvalidException {
        BunqRequestBuilder requestBuilder;
        String content = mapObjectToJsonString(data);
        BunqRequestBody requestBody = BunqRequestBody.create(ContentType.JSON.getMediaType(), content.getBytes(StandardCharsets.UTF_8));
        requestBuilder = new BunqRequestBuilder().post(requestBody);
        requestBuilder.url(fullPath);

        HttpHeaders headers = headerProducer.getSignedHeaders(keyPair, clientAuthentication, requestBuilder);
        HttpEntity<Object> requestEntity = new HttpEntity<>(content, headers);
        return exchange(fullPath, HttpMethod.POST, requestEntity, prometheusMater, responseType);
    }

    void delete(final String fullPath,
                final KeyPair keyPair,
                final String clientAuthentication,
                final String prometheusMater) throws TokenInvalidException {
        BunqRequestBuilder requestBuilder = new BunqRequestBuilder().get();
        requestBuilder.url(fullPath);

        HttpHeaders headers = headerProducer.getSignedHeaders(keyPair, clientAuthentication, requestBuilder);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        exchange(fullPath, HttpMethod.DELETE, requestEntity, prometheusMater, String.class);
    }

    @Override
    public <T> ResponseEntity<T> exchange(final String endpoint,
                                           final HttpMethod method,
                                           final HttpEntity body,
                                           final String prometheusPathOverride,
                                           final Class<T> responseType,
                                           final String... uriArgs) throws TokenInvalidException {
        return exchange(endpoint, method, body, prometheusPathOverride, responseType,
                BUNQ_HTTP_ERROR_HANDLER, uriArgs);
    }

    @Override
    public <T> T exchangeForBody(final String endpoint,
                                  final HttpMethod method,
                                  final HttpEntity body,
                                  final String prometheusPathOverride,
                                  final Class<T> responseType,
                                  final String... uriArgs) throws TokenInvalidException {
        return exchange(endpoint, method, body, prometheusPathOverride, responseType,
                BUNQ_HTTP_ERROR_HANDLER, uriArgs).getBody();
    }

    private String mapObjectToJsonString(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BunqPostBodyMalformedException();
        }
    }
}
