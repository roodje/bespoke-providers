package com.yolt.providers.bunq.common.http;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.security.KeyPair;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BunqHttpHeaderProducerTest {

    @Mock
    private ObjectMapper objectMapper;
    private BunqHttpHeaderProducer httpHeaderProducer;

    @BeforeEach
    void setUp() {
        httpHeaderProducer = new BunqHttpHeaderProducer(objectMapper);
    }

    @Test
    void shouldReturnedSignedHttpHeaders() throws JsonProcessingException {
        //given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String clientAuthorization = UUID.randomUUID().toString();
        String mockedRequestBody = "mocked-request-body";
        String url = "https://bunq.api.com/some-endpoint";
        when(objectMapper.writeValueAsString(mockedRequestBody)).thenReturn("mocked-serialized-request-body");

        //when
        HttpHeaders returnedHeaders = httpHeaderProducer.getSignedHeaders(keyPair, clientAuthorization, mockedRequestBody, url);

        //then
        assertThat(returnedHeaders.toSingleValueMap()).containsAllEntriesOf(Map.of(
                "Cache-Control", "no-cache",
                "User-Agent", "yolt-user-agent",
                "X-Bunq-Language", "en_US",
                "X-Bunq-Region", "nl_NL",
                "X-Bunq-Geolocation", "0 0 0 0 000",
                "X-Bunq-Client-Authentication", clientAuthorization
        )).containsKeys("X-Bunq-Client-Request-Id", "X-Bunq-Client-Signature");
    }
}