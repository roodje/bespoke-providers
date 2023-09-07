package com.yolt.providers.stet.generic.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import lombok.SneakyThrows;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationMeansInterpreterTest {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String PROVIDER_IDENTIFIER = "EXAMPLE_PROVIDER";
    private static final String CLIENT_ID_NAME = "client-id";
    private static final String CLIENT_SECRET_NAME = "client-secret";
    private static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-key-id";
    private static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String CLIENT_TRANSPORT_KEY_ID = "4a7f5aad-e2a1-465a-8da4-d4462ccd3900";

    @Test
    void shouldInterpretDataCorrectlyFromAuthenticationMeans() {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>();
        authMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), CLIENT_SECRET));
        authMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), CLIENT_TRANSPORT_KEY_ID));
        authMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificatePem()));

        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(authMeans, PROVIDER_IDENTIFIER);

        // when
        String clientId = interpreter.getValue(CLIENT_ID_NAME);
        String clientSecret = interpreter.getNullableValue(CLIENT_SECRET_NAME);
        UUID clientTransportKeyId = interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID_NAME);
        X509Certificate clientTransportCertificate = interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE_NAME);

        // then
        assertThat(clientId).isEqualTo(CLIENT_ID);
        assertThat(clientSecret).isEqualTo(CLIENT_SECRET);
        assertThat(clientTransportKeyId).isEqualTo(UUID.fromString(CLIENT_TRANSPORT_KEY_ID));
        assertThat(clientTransportCertificate).isNotNull();
    }

    @Test
    void shouldFailDueToMissingAuthenticationMeans() {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>();
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(authMeans, PROVIDER_IDENTIFIER);

        // when
        ThrowableAssert.ThrowingCallable throwingCallableMissingClientId = () -> interpreter.getValue(CLIENT_ID_NAME);
        ThrowableAssert.ThrowingCallable throwingCallableMissingClientTransportKeyId = () -> interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID_NAME);
        ThrowableAssert.ThrowingCallable throwingCallableClientTransportCertificate = () -> interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE_NAME);

        // then
        assertThatThrownBy(throwingCallableMissingClientId).isInstanceOf(MissingAuthenticationMeansException.class);
        assertThatThrownBy(throwingCallableMissingClientTransportKeyId).isInstanceOf(MissingAuthenticationMeansException.class);
        assertThatThrownBy(throwingCallableClientTransportCertificate).isInstanceOf(MissingAuthenticationMeansException.class);
    }

    @Test
    void shouldNotFailDueToMissingAuthenticationMean() {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>();
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(authMeans, PROVIDER_IDENTIFIER);

        // when
        String clientSecret = interpreter.getNullableValue(CLIENT_SECRET_NAME);

        // then
        assertThat(clientSecret).isNull();
    }

    @SneakyThrows
    private String readCertificatePem() {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}
