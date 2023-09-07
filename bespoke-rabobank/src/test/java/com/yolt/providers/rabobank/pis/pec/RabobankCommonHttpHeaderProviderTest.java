package com.yolt.providers.rabobank.pis.pec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Clock;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class RabobankCommonHttpHeaderProviderTest {

    private RabobankCommonHttpHeaderProvider subject;

    @BeforeEach
    void setUp() {
        subject = new RabobankCommonHttpHeaderProvider(Clock.systemDefaultZone());
    }

    @Test
    void shouldReturnCommonHttpHeaders() {
        //given
        String clientId = "someClientId";
        String psuIpAddress = "127.1.1.1";

        //when
        HttpHeaders returnedHeaders = subject.providerCommonHttpHeaders(psuIpAddress, clientId);

        //then
        assertThat(returnedHeaders).containsAllEntriesOf(Map.of(
                HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE),
                "PSU-IP-Address", Collections.singletonList(psuIpAddress),
                "x-ibm-client-id", Collections.singletonList(clientId)));

        assertThat(returnedHeaders.get("date")).satisfies(hs -> assertThat(hs).hasSize(1))
                .satisfies(hs -> assertThat(hs.get(0)).containsPattern(Pattern.compile("[A-Za-z]{3}, \\d{2} [A-Za-z]{3} \\d{4} \\d{2}:\\d{2}:\\d{2} [A-Z]{3}")));
        assertThat(returnedHeaders.get("x-request-id")).satisfies(hs -> assertThat(hs).hasSize(1))
                .satisfies(hs -> assertThat(hs.get(0)).matches(Pattern.compile("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}")));
    }
}
