package com.yolt.providers.openbanking.ais.santander.http;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.jwx.Headers;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class SantanderHttpPayloadSignerTest {

    private static final String KEY_ID = "KeyId";
    private SantanderHttpPayloadSignerV2 subject;

    @Mock
    private DefaultAuthMeans authMeans;

    @BeforeEach
    public void setUp() {
        subject = new SantanderHttpPayloadSignerV2(null, AlgorithmIdentifiers.RSA_USING_SHA256);
    }

    @Test
    public void shouldNotContainB64InJws() {
        // given
        JsonWebSignature jws = new JsonWebSignature();
        jws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, false);
        jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);
        jws.setKeyIdHeaderValue(null);

        // when
        subject.adjustJWSHook(jws, authMeans);

        // then
        String headersAsString = jws.getHeaders().getFullHeaderAsJsonString();
        assertThat(headersAsString).doesNotContain("b64");
        assertThat(headersAsString).doesNotContain("kid");
        assertThat(headersAsString).contains("http://openbanking.org.uk/tan");
        assertThat(headersAsString).contains("http://openbanking.org.uk/iss");
        assertThat(headersAsString).contains("http://openbanking.org.uk/tan");
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenB64HasIncorrectValue() {
        // given
        JsonWebSignature jws = new JsonWebSignature();
        jws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, true);
        jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);

        // when -> then
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> subject.adjustJWSHook(jws, authMeans))
                .withMessage("Wrong format of Santander payment claims: adjusted headers should contain b64 headers with value false");
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenB64IsMissing() {
        // given
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);

        // when -> then
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> subject.adjustJWSHook(jws, authMeans))
                .withMessage("Wrong format of Santander payment claims: adjusted headers should contain b64 headers with value false");
    }

    @Test
    public void shouldKeyIdIsNotRemovedIfExists() {
        // given
        JsonWebSignature jws = new JsonWebSignature();
        jws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, false);
        jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);
        jws.setKeyIdHeaderValue(KEY_ID);

        // when
        subject.adjustJWSHook(jws, authMeans);

        // then
        assertThat(jws.getKeyIdHeaderValue()).isEqualTo(KEY_ID);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenIncorrectFormat() throws JoseException {
        // given
        JsonWebSignature jws = createJoseThrowingExceptionJWS();

        // when -> then
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> subject.adjustJWSHook(jws, authMeans))
                .withMessage("Error during removing b64 header");
    }

    private JsonWebSignature createJoseThrowingExceptionJWS() throws JoseException {
        Headers headers = mock(Headers.class);
        when(headers.getFullHeaderAsJsonString()).thenReturn("{\"b64\":false, \"kid\":\"kidValue\"}");
        when(headers.getObjectHeaderValue("b64")).thenReturn(false);
        doThrow(JoseException.class).when(headers).setFullHeaderAsJsonString(eq("{ \"kid\":\"kidValue\"}"));
        JsonWebSignature jws = mock(JsonWebSignature.class);
        when(jws.getHeaders()).thenReturn(headers);
        return jws;
    }
}