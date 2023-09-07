package com.yolt.providers.monorepogroup.qontogroup.common.mapper.authmeans;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth.DefaultQontoGroupAuthenticationMeansMapper;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DefaultQontoGroupAuthenticationMeansMapperTest {

    private static final String CERTIFICATE_PEM = """
            -----BEGIN CERTIFICATE-----
            MIICMzCCAZygAwIBAgIJALiPnVsvq8dsMA0GCSqGSIb3DQEBBQUAMFMxCzAJBgNV
            BAYTAlVTMQwwCgYDVQQIEwNmb28xDDAKBgNVBAcTA2ZvbzEMMAoGA1UEChMDZm9v
            MQwwCgYDVQQLEwNmb28xDDAKBgNVBAMTA2ZvbzAeFw0xMzAzMTkxNTQwMTlaFw0x
            ODAzMTgxNTQwMTlaMFMxCzAJBgNVBAYTAlVTMQwwCgYDVQQIEwNmb28xDDAKBgNV
            BAcTA2ZvbzEMMAoGA1UEChMDZm9vMQwwCgYDVQQLEwNmb28xDDAKBgNVBAMTA2Zv
            bzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAzdGfxi9CNbMf1UUcvDQh7MYB
            OveIHyc0E0KIbhjK5FkCBU4CiZrbfHagaW7ZEcN0tt3EvpbOMxxc/ZQU2WN/s/wP
            xph0pSfsfFsTKM4RhTWD2v4fgk+xZiKd1p0+L4hTtpwnEw0uXRVd0ki6muwV5y/P
            +5FHUeldq+pgTcgzuK8CAwEAAaMPMA0wCwYDVR0PBAQDAgLkMA0GCSqGSIb3DQEB
            BQUAA4GBAJiDAAtY0mQQeuxWdzLRzXmjvdSuL9GoyT3BF/jSnpxz5/58dba8pWen
            v3pj4P3w5DoOso0rzkZy2jEsEitlVM2mLSbQpMM+MUVQCQoiG6W9xuCFuxSrwPIS
            pAqEAuV4DNoxQKKWmhVv+J0ptMWD25Pnpxeq5sXzghfJnslJlQND
            -----END CERTIFICATE-----""";

    private final DefaultQontoGroupAuthenticationMeansMapper authMeansMapper = new DefaultQontoGroupAuthenticationMeansMapper("https://s3baseurl.com");

    @Test
    void shouldCreateAuthenticationMeans() throws CertificateException {
        //given
        Map<String, BasicAuthenticationMean> authMeanMap = new HashMap<>();
        authMeanMap.put(CLIENT_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "THE-CLIENT-ID"));
        authMeanMap.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SECRET_STRING.getType(), "THE-CLIENT-SECRET"));
        authMeanMap.put(SIGNING_CERTIFICATE_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), "da584654-09b7-11ed-861d-0242ac120002"));
        authMeanMap.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), CERTIFICATE_PEM));
        var expectedAuthMeans = new QontoGroupAuthenticationMeans(
                new SigningData("https://s3baseurl.com/5f:18:d2:93:04:36:f7:d3:8b:91:11:d8:85:ac:62:0c:06:e9:54:02.pem", UUID.fromString("da584654-09b7-11ed-861d-0242ac120002")),
                "THE-CLIENT-ID",
                "THE-CLIENT-SECRET");

        //when
        var result = authMeansMapper.map(authMeanMap, "PROVIDER_KEY");

        //then
        assertThat(result).isEqualTo(expectedAuthMeans);
    }

    @Test
    void shouldThrowMissingAuthenticationMeansExceptionWhenAtLeasOneAuthenticationMeanIsMissing() throws CertificateException {
        //given
        Map<String, BasicAuthenticationMean> authMeanMap = new HashMap<>();
        authMeanMap.put(CLIENT_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "THE-CLIENT-ID"));
        authMeanMap.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SECRET_STRING.getType(), "THE-CLIENT-SECRET"));
        authMeanMap.put(SIGNING_CERTIFICATE_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), "da584654-09b7-11ed-861d-0242ac120002"));
        //when
        ThrowableAssert.ThrowingCallable call = () -> authMeansMapper.map(authMeanMap, "PROVIDER_KEY");

        //then
        assertThatExceptionOfType(MissingAuthenticationMeansException.class)
                .isThrownBy(call)
                .withMessage("Missing authentication mean for PROVIDER_KEY, authenticationKey=" + SIGNING_CERTIFICATE_NAME);
    }

    @Test
    void shouldThrowInvalidAuthenticationMeansExceptionWhenCertificateIsMalformed() throws CertificateException {
        //given
        Map<String, BasicAuthenticationMean> authMeanMap = new HashMap<>();
        authMeanMap.put(CLIENT_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "THE-CLIENT-ID"));
        authMeanMap.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SECRET_STRING.getType(), "THE-CLIENT-SECRET"));
        authMeanMap.put(SIGNING_CERTIFICATE_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), "da584654-09b7-11ed-861d-0242ac120002"));
        authMeanMap.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), "Malformed certificate"));
        //when
        ThrowableAssert.ThrowingCallable call = () -> authMeansMapper.map(authMeanMap, "PROVIDER_KEY");

        //then
        assertThatExceptionOfType(InvalidAuthenticationMeansException.class)
                .isThrownBy(call)
                .withMessage("Invalid authentication mean for PROVIDER_KEY, authenticationKey=" + SIGNING_CERTIFICATE_NAME + "errorMessage=Cannot process certificate for thumbprint");
    }
}