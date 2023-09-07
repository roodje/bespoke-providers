package com.yolt.providers.rabobank.pis.pec;

import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.mock.SignerMock;
import com.yolt.providers.rabobank.RabobankSampleTypedAuthenticationMeans;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RabobankPisHeadersSignerTest {

    @InjectMocks
    private RabobankPisHeadersSigner subject;

    private SignerMock signerMock = new SignerMock();

    private static RabobankSampleTypedAuthenticationMeans sampleTypedAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();

    @Test
    void shouldReturnedHeadersWithAddedSignature() throws CertificateException, IOException, URISyntaxException {
        //given
        HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.add("date", "Thu, 20 May 2021 13:47:50 GMT+2");
        givenHeaders.add("tpp-redirect-uri", "http://redirect.com/");
        givenHeaders.add("tpp-nok-redirect-uri", "http://redirect.com/nok");
        givenHeaders.add("x-request-id", "8f4b56d2-01ef-4a1d-b775-935b01d33c94");

        //when
        HttpHeaders signedHeaders = subject.signHeaders(givenHeaders, new byte[]{}, signerMock, UUID.randomUUID(), createCertificate());

        //then

        assertThat(signedHeaders).
                containsAllEntriesOf(givenHeaders)
                .containsAllEntriesOf(Map.of(
                        "digest", Collections.singletonList("sha-512=z4PhNX7vuL3xVChQ1m2AB9Yg5AULVxXcg/SpIdNs6c5H0NE8XYXysP+DGNKHfuwvY7kxvUdBeoGlODJ6+SfaPg=="),
                        "tpp-signature-certificate", Collections.singletonList("MIIDFjCCAf4CCQC6MIKO2WnC1TANBgkqhkiG9w0BAQsFADBSMQswCQYDVQQGEwJOTDEWMBQGA1UECAwNTm9vcmQtSG9sbGFuZDENMAsGA1UECgwEWW9sdDEcMBoGCSqGSIb3DQEJARYNbGVvbkB5b2x0LmNvbTAeFw0xODA5MjAwODUzMTVaFw0yMTA3MTAwODUzMTVaMEgxCzAJBgNVBAYTAk5MMRswGQYDVQQKDBJZb2x0IC0gQ29ubmVjdGlvbnMxHDAaBgkqhkiG9w0BCQEWDWxlb25AeW9sdC5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDXu4pG9Fdvek5opDjk9DsgMe+vZsYVoHP3E4fDa0O9n06+wFh1qgrbctu3UNKhTiqiOuyqar9IkSIV92GO09xIjyxAWGVv8eNN2poGVRDta6zo2YLanmU1EuXcMd5hICj3l/PEJbsksFP2jNlsJsfE6ehSCdrPKcX9oYL3oCvZDohytdrHO4c+8m8/aZS1nQyhT9lyqWMcBvz9I35BE2uiVPiBKuAtZZcp0yHvvObNC/BzrZncM2LrrtCDcS6ozjotHvCbk37W+loBr6b1+jA64qv3ILDl+T7fcmUyIyBoWa5GVdmZ9Go8Gl3SOcjGx2SwtmC1V6w4DDbZkLyT6oJLAgMBAAEwDQYJKoZIhvcNAQELBQADggEBABZPEHktIG+pKoOfpeBsRF6K5ZIBbmSeRBrzJ0BOOUhr894iuHhMKku239fN3sFc0+6ylQjjFC3soBimWVcnf6AUb8ncP4FCE8oyHpZ2WPjYeXqIdTxxFPfA0l3FW18ncdIa0zSFmo5wwzhN7A2FhtCfnlI8YkatHQSv/90VMuICMZF0HTzfeuhv4z8PH6SVEa3B1uYogwwv9dCW/8amOddoO2K/aB4hS23h4TyAw1tKjN8/IHmLvE3R8LlazMJhY9J2xvYOPFq3BnM3V8vHpj/7BBWJKxWvYtbCqqVCEeG/qLQH7CmVaIX7hg5Yfurwvgwm3mujCaRNKjHjS3Jb5YQ="),
                        "signature", Collections.singletonList("keyId=\"13416366839981261525\",algorithm=\"rsa-sha512\",headers=\"date digest tpp-nok-redirect-uri tpp-redirect-uri x-request-id\",signature=\"ZGF0ZTogVGh1LCAyMCBNYXkgMjAyMSAxMzo0Nzo1MCBHTVQrMgpkaWdlc3Q6IHNoYS01MTI9ejRQaE5YN3Z1TDN4VkNoUTFtMkFCOVlnNUFVTFZ4WGNnL1NwSWROczZjNUgwTkU4WFlYeXNQK0RHTktIZnV3dlk3a3h2VWRCZW9HbE9ESjYrU2ZhUGc9PQp0cHAtbm9rLXJlZGlyZWN0LXVyaTogaHR0cDovL3JlZGlyZWN0LmNvbS9ub2sKdHBwLXJlZGlyZWN0LXVyaTogaHR0cDovL3JlZGlyZWN0LmNvbS8KeC1yZXF1ZXN0LWlkOiA4ZjRiNTZkMi0wMWVmLTRhMWQtYjc3NS05MzViMDFkMzNjOTQ=\"")
                ));
    }

    private static X509Certificate createCertificate() throws IOException, URISyntaxException, CertificateException {
        return KeyUtil.createCertificateFromPemFormat(sampleTypedAuthenticationMeans.readFakeCertificatePem());
    }
}
