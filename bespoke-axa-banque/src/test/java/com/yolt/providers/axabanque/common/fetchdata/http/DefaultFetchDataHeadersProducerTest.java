package com.yolt.providers.axabanque.common.fetchdata.http;

import com.yolt.providers.axabanque.common.fetchdata.http.headerproducer.DefaultFetchDataHeadersProducer;
import com.yolt.providers.axabanque.common.fetchdata.http.headerproducer.FetchDataRequestHeadersProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultFetchDataHeadersProducerTest {

    private FetchDataRequestHeadersProducer headersProducer;

    @BeforeEach
    public void setup() {
        headersProducer = new DefaultFetchDataHeadersProducer();
    }

    @Test
    public void shouldReturnGetAccountHeaders() {
        //given
        Map<? extends String, ? extends String> expected = new HashMap<String, String>() {{
            put("consent-id", "consentId");
            put("Authorization", "Bearer accessToken");
            put("PSU-IP-Address", "127.0.1.2");
        }};
        //when
        HttpHeaders headers = headersProducer.getAccountsHeaders("accessToken", "consentId", "xRequestId", "127.0.1.2");
        //then
        assertThat(headers).hasSize(4);
        assertThat(headers.toSingleValueMap())
                .containsAllEntriesOf(expected);
        assertThat(headers).containsKey("x-request-id");
    }

    @Test
    public void shouldReturnGetTransactionsHeaders() {
        //given
        Map<? extends String, ? extends String> expected = new HashMap<String, String>() {{
            put("consent-id", "consentId");
            put("Authorization", "Bearer accessToken");
            put("PSU-IP-Address", "127.0.1.2");
        }};
        //when
        HttpHeaders headers = headersProducer.getTransactionsHeaders("accessToken", "consentId", "xRequestId", "127.0.1.2");
        //then
        assertThat(headers).hasSize(4);
        assertThat(headers.toSingleValueMap())
                .containsAllEntriesOf(expected);
        assertThat(headers).containsKey("x-request-id");
    }

    @Test
    public void shouldReturnGetAccountHeadersWithoutPsuIpAddress() {
        //given
        Map<? extends String, ? extends String> expected = new HashMap<String, String>() {{
            put("consent-id", "consentId");
            put("Authorization", "Bearer accessToken");
        }};
        //when
        HttpHeaders headers = headersProducer.getAccountsHeaders("accessToken", "consentId", "xRequestId", null);
        //then
        assertThat(headers).hasSize(3);
        assertThat(headers.toSingleValueMap())
                .containsAllEntriesOf(expected);
        assertThat(headers).containsKey("x-request-id");
        assertThat(headers).doesNotContainKey("PSU-IP-Address");
    }

    @Test
    public void shouldReturnGetTransactionsHeadersWithoutPsuIpAddress() {
        //given
        Map<? extends String, ? extends String> expected = new HashMap<String, String>() {{
            put("consent-id", "consentId");
            put("Authorization", "Bearer accessToken");
        }};
        //when
        HttpHeaders headers = headersProducer.getTransactionsHeaders("accessToken", "consentId", "xRequestId", null);
        //then
        assertThat(headers).hasSize(3);
        assertThat(headers.toSingleValueMap())
                .containsAllEntriesOf(expected);
        assertThat(headers).containsKey("x-request-id");
        assertThat(headers).doesNotContainKey("PSU-IP-Address");
    }


}
