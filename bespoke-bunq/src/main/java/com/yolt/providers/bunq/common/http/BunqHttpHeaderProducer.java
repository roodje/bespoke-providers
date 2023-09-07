package com.yolt.providers.bunq.common.http;

import com.bunq.sdk.http.BunqRequestBody;
import com.bunq.sdk.http.BunqRequestBuilder;
import com.bunq.sdk.http.ContentType;
import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class BunqHttpHeaderProducer {

    private final ObjectMapper objectMapper;

    private static final String GEOLOCATION_UNKNOWN = "0 0 0 0 000";
    private static final String NO_CACHE = "no-cache";

    // XXX this cannot be used in the interceptor because our signature is dependent on the headers
    public HttpHeaders getMandatoryHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", NO_CACHE);
        headers.add("User-Agent", "yolt-user-agent");
        headers.add("X-Bunq-Language", "en_US");
        headers.add("X-Bunq-Region", "nl_NL");
        headers.add("X-Bunq-Client-Request-Id", ExternalTracingUtil.createLastExternalTraceId());
        headers.add("X-Bunq-Geolocation", GEOLOCATION_UNKNOWN);
        return headers;
    }

    public HttpHeaders getSignedHeaders(KeyPair keyPair, String clientAuthentication, BunqRequestBuilder requestBuilder) {
        HttpHeaders headers = getMandatoryHttpHeaders();
        headers.add("X-Bunq-Client-Authentication", clientAuthentication);
        Set<Map.Entry<String, List<String>>> headersSet = headers.entrySet();
        // XXX we don't have arrays in our headers so just concat into a single string.
        headersSet.forEach(it -> requestBuilder.addHeader(it.getKey(), String.join("", it.getValue())));
        headers.add("X-Bunq-Client-Signature", SecurityUtils.generateSignature(requestBuilder, keyPair));
        return headers;
    }

    public HttpHeaders getSignedHeaders(KeyPair keyPair, String clientAuthentication, Object requestBodyObject, String fullUrl) throws JsonProcessingException {
        BunqRequestBuilder requestBuilder;
        String content = objectMapper.writeValueAsString(requestBodyObject);
        BunqRequestBody requestBody = BunqRequestBody.create(ContentType.JSON.getMediaType(), content.getBytes(StandardCharsets.UTF_8));
        requestBuilder = new BunqRequestBuilder().post(requestBody);
        requestBuilder.url(fullUrl);
        return getSignedHeaders(keyPair, clientAuthentication, requestBuilder);
    }
}
