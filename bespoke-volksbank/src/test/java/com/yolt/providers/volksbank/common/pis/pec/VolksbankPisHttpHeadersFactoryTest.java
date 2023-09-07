package com.yolt.providers.volksbank.common.pis.pec;

import com.yolt.providers.volksbank.common.util.HttpUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VolksbankPisHttpHeadersFactoryTest {

    @InjectMocks
    private VolksbankPisHttpHeadersFactory subject;

    @Test
    void shouldReturnProperHttpHeadersForCreatePaymentInitiationHttpHeadersWhenCorrectData() {
        // given
        var clientId = "fakeClientId";
        var psuIpAddress = "psuIpAddress";

        // when
        var result = subject.createPaymentInitiationHttpHeaders(clientId, psuIpAddress);

        // then
        assertThat(result.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.AUTHORIZATION, "fakeClientId",
                "PSU-IP-Address", "psuIpAddress"
        ));
    }

    @Test
    void shouldReturnProperHttpHeadersForCreateCommonHttpHeadersWhenCorrectData() {
        // given
        var clientId = "fakeClientId";

        // when
        var result = subject.createCommonHttpHeaders(clientId);

        // then
        assertThat(result.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.AUTHORIZATION, "fakeClientId"
        ));
    }

    @Test
    void shouldReturnProperHttpHeadersForCreateAccessTokenHttpHeadersWhenCorrectData() {
        // given
        var clientId = "fakeClientId";
        var clientSecret = "fakeClientSecret";

        // when
        var result = subject.createAccessTokenHttpHeaders(clientId, clientSecret);

        // then
        assertThat(result.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                HttpHeaders.AUTHORIZATION, HttpUtils.basicCredentials(clientId, clientSecret)
        ));
    }
}