package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefaultRaiffeisenAtGroupHttpHeadersProducerTest {

    private DefaultRaiffeisenAtGroupHttpHeadersProducer headersProducer = new DefaultRaiffeisenAtGroupHttpHeadersProducer();

    @Test
    void shouldCreateClientCredentialTokenHttpHeaders() {
        //given
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //when
        var receivedHeaders = headersProducer.createClientCredentialTokenHttpHeaders();

        //then
        assertThat(receivedHeaders).isEqualTo(expectedHeaders);
    }

    @Test
    void shouldCreateUserConsentHeadersWithPsuIpAddress() {
        //given
        var clientToken = "TheClientToken";
        var redirectUrl = "https://redirecturl.com";
        var psuIpAddress = "127.0.0.1";
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setBearerAuth(clientToken);
        expectedHeaders.set("TPP-Redirect-URI", redirectUrl);
        expectedHeaders.add("PSU-IP-Address", psuIpAddress);

        //when
        var receivedHeaders = headersProducer.createUserConsentHeaders(clientToken, redirectUrl, psuIpAddress);

        //then
        assertThat(receivedHeaders).isEqualTo(expectedHeaders);
    }

    @Test
    void shouldCreateUserConsentHeadersWithoutPsuIpAddress() {
        //given
        var clientToken = "TheClientToken";
        var redirectUrl = "https://redirecturl.com";
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setBearerAuth(clientToken);
        expectedHeaders.set("TPP-Redirect-URI", redirectUrl);

        //when
        var receivedHeaders = headersProducer.createUserConsentHeaders(clientToken, redirectUrl, null);

        //then
        assertThat(receivedHeaders).isEqualTo(expectedHeaders);
    }

    @Test
    void shouldCreateGetUserConsentHeadersWithPsuIpdAddress() {
        //given
        var clientToken = "TheClientToken";
        var psuIpAddress = "127.0.0.1";
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setBearerAuth(clientToken);
        expectedHeaders.set("PSU-IP-Address", psuIpAddress);

        //when
        var result = headersProducer.getUserConsentHeaders(clientToken, psuIpAddress);

        //then
        assertThat(result).isEqualTo(expectedHeaders);
    }

    @Test
    void shouldCreateGetUserConsentHeadersWithoutPsuIpdAddress() {
        //given
        var clientToken = "TheClientToken";
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setBearerAuth(clientToken);

        //when
        var result = headersProducer.getUserConsentHeaders(clientToken, null);

        //then
        assertThat(result).isEqualTo(expectedHeaders);
    }

    @Test
    void shouldCreateDeleteUserConsentHeadersWithPsuIpAddress() {
        //given
        var clientToken = "TheClientToken";
        var psuIpAddress = "127.0.0.1";
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setBearerAuth(clientToken);
        expectedHeaders.set("PSU-IP-Address", psuIpAddress);

        //when
        var result = headersProducer.deleteUserConsentHeaders(clientToken, psuIpAddress);

        //then
        assertThat(result).isEqualTo(expectedHeaders);
    }

    @Test
    void shouldCreateDeleteUserConsentHeadersWithoutPsuIpAddress() {
        //given
        var clientToken = "TheClientToken";
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setBearerAuth(clientToken);

        //when
        var result = headersProducer.deleteUserConsentHeaders(clientToken, null);

        //then
        assertThat(result).isEqualTo(expectedHeaders);
    }

    @Test
    void shouldCreateFetchDataHeadersWithPsuIdAddress() {
        //given
        var clientToken = "TheClientToken";
        var consentId = "TheConsentId";
        var psuIpAddress = "127.0.0.1";
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setBearerAuth(clientToken);
        expectedHeaders.set("PSU-IP-Address", psuIpAddress);
        expectedHeaders.set("Consent-ID", consentId);

        //when
        var result = headersProducer.getFetchDataHeaders(clientToken, consentId, psuIpAddress);

        //then
        assertThat(result).isEqualTo(expectedHeaders);
    }

    @Test
    void shouldCreateFetchDataHeadersWithoutPsuIdAddress() {
        //given
        var clientToken = "TheClientToken";
        var consentId = "TheConsentId";
        var expectedHeaders = new HttpHeaders();
        expectedHeaders.setBearerAuth(clientToken);
        expectedHeaders.set("Consent-ID", consentId);

        //when
        var result = headersProducer.getFetchDataHeaders(clientToken, consentId, null);

        //then
        assertThat(result).isEqualTo(expectedHeaders);
    }
}