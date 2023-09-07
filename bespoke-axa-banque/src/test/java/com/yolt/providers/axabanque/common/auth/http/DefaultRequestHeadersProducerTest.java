package com.yolt.providers.axabanque.common.auth.http;

import com.yolt.providers.axabanque.common.auth.http.headerproducer.AuthorizationRequestHeadersProducer;
import com.yolt.providers.axabanque.common.auth.http.headerproducer.DefaultAuthorizationRequestHeadersProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.contract.spec.internal.MediaTypes.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

public class DefaultRequestHeadersProducerTest {
    private AuthorizationRequestHeadersProducer axaGroupRequestHeadersProducer;

    @BeforeEach
    public void setup() {
        axaGroupRequestHeadersProducer = new DefaultAuthorizationRequestHeadersProducer();
    }

    @Test
    public void shouldReturnAuthorizationRequestHeaders() {
        //given
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.put(ACCEPT, Collections.singletonList(APPLICATION_JSON));
        expectedHeaders.put("x-request-id", Collections.singletonList("xRequestId"));
        //when
        HttpHeaders headers = axaGroupRequestHeadersProducer.createAuthorizationResourceHeaders("xRequestId");
        //then
        assertThat(headers).isEqualToComparingFieldByField(expectedHeaders);
    }

    @Test
    public void shouldReturnTokenRequestHeaders() {
        //given
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.put(ACCEPT, Collections.singletonList(APPLICATION_JSON));
        expectedHeaders.put(CONTENT_TYPE, Collections.singletonList(APPLICATION_FORM_URLENCODED.toString()));
        //when
        HttpHeaders headers = axaGroupRequestHeadersProducer.createTokenHeaders();
        //then
        assertThat(headers).containsExactlyInAnyOrderEntriesOf(expectedHeaders);
    }

    @Test
    public void shouldReturnConsentCreationHeaders() {
        //given
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.put(ACCEPT, Collections.singletonList(APPLICATION_JSON));
        expectedHeaders.put(CONTENT_TYPE, Collections.singletonList(APPLICATION_JSON));
        expectedHeaders.put("tpp-redirect-uri", Collections.singletonList("redirectUrl"));
        expectedHeaders.put("psu-ip-address", Collections.singletonList("psuIdAddress"));
        expectedHeaders.put("x-request-id", Collections.singletonList("xRequestId"));
        //when
        HttpHeaders headers = axaGroupRequestHeadersProducer.createConsentCreationHeaders("redirectUrl", "psuIdAddress", "xRequestId");
        //then
        assertThat(headers).isEqualToComparingFieldByField(expectedHeaders);
    }
}
