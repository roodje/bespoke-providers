package com.yolt.providers.rabobank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import static com.yolt.securityutils.signing.SignatureAlgorithm.SHA256_WITH_RSA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.*;

/**
 * This test class verifies logic responsible for creating requests needed to create production account on Rabobank portal.
 * <p>
 * Disclaimer: {@link JwsRequestTppEnrollment} is class responsible for this process so it is used for testing
 * <p>
 * Covered flows:
 * - sending JWS request for TPP enrollment
 * <p>
 */
class JwsRequestTppEnrollmentTest {

    @Test
    void shouldSuccessfullySendJwsRequestTppEnrollmentForSendJwsRequestTppEnrollmentWithCorrectData() throws CertificateEncodingException {
        // given
        UUID kid = new UUID(0, 0);
        X509Certificate cert = Mockito.mock(X509Certificate.class);
        Signer signer = Mockito.mock(Signer.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Clock fixedClock = Clock.fixed(Instant.parse("2018-04-29T10:15:30.00Z"), ZoneId.of("Europe/Amsterdam"));
        when(cert.getEncoded()).thenReturn(new byte[0]);
        when(cert.getSubjectDN()).thenReturn(() -> "sjef");
        when(signer.sign(aryEq("eyJhbGciOiJSUzI1NiIsIng1YyI6WyIiXX0.eyJwdGNfZW1haWwiOiJzamFha0BleGFtcGxlLmNvbSIsImV4cCI6MH0".getBytes()), eq(kid), eq(SHA256_WITH_RSA)))
                .thenReturn(Base64.getEncoder().encodeToString("krabbel".getBytes()));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenAnswer((Answer<ResponseEntity<Void>>) invocation -> {
                    if (!"https://api.rabobank.nl/openapi/open-banking/third-party-providers".equals(invocation.getArgument(0))) {
                        return notFound().build();
                    }
                    HttpEntity<?> entity = invocation.getArgument(2);
                    if (!"64f38624-718d-4732-b579-b8979071fcb0".equals(entity.getHeaders().getFirst("x-ibm-client-id"))) {
                        return badRequest().build();
                    }
                    return accepted().build();
                });

        // when
        boolean success = JwsRequestTppEnrollment.sendJwsRequestTppEnrollment("sjaak@example.com", cert, kid, signer, restTemplate, Instant.EPOCH, fixedClock);

        // then
        assertThat(success).isTrue();
    }

    @Test
    void shouldSuccessfullySendJwsRequestTppEnrollmentForSendJwsRequestTppEnrollmentWhenExpiryTimeIsMissing() throws CertificateEncodingException {
        // given
        UUID kid = new UUID(0, 0);
        X509Certificate cert = Mockito.mock(X509Certificate.class);
        Signer signer = Mockito.mock(Signer.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Clock fixedClock = Clock.fixed(Instant.parse("2018-04-29T10:15:30.00Z"), ZoneId.of("Europe/Amsterdam"));
        when(cert.getEncoded()).thenReturn(new byte[0]);
        when(cert.getSubjectDN()).thenReturn(() -> "sjef");
        when(signer.sign(aryEq("eyJhbGciOiJSUzI1NiIsIng1YyI6WyIiXX0.eyJwdGNfZW1haWwiOiJzamFha0BleGFtcGxlLmNvbSIsImV4cCI6MTUyNTYwMTczMH0".getBytes()), eq(kid), eq(SHA256_WITH_RSA)))
                .thenReturn(Base64.getEncoder().encodeToString("krabbel".getBytes()));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenAnswer((Answer<ResponseEntity<Void>>) invocation -> {
                    if (!"https://api.rabobank.nl/openapi/open-banking/third-party-providers".equals(invocation.getArgument(0))) {
                        return notFound().build();
                    }
                    HttpEntity<Request> entity = invocation.getArgument(2);
                    if (!"64f38624-718d-4732-b579-b8979071fcb0".equals(entity.getHeaders().getFirst("x-ibm-client-id"))) {
                        return badRequest().build();
                    }
                    if (!new String(Base64.getDecoder().decode(entity.getBody().getPayload().getBytes())).contains("\"exp\":1525601730")) {
                        return badRequest().build();
                    }
                    return accepted().build();
                });

        // when
        boolean success = JwsRequestTppEnrollment.sendJwsRequestTppEnrollment("sjaak@example.com", cert, kid, signer, restTemplate, null, fixedClock);

        // then
        assertThat(success).isTrue();
    }

    @Test
    void shouldMapRequestObjectIntoJsonUsingWriteValueAsStringWithCorrectData() throws JsonProcessingException {
        // given
        ObjectMapper objectMapper = new ObjectMapper();

        // when
        String json = objectMapper.writeValueAsString(new Request("1", "2", "3"));

        // then
        assertThat(json).contains("\"protected\"");
        assertThat(json).doesNotContain("\"protected_\"");
    }
}