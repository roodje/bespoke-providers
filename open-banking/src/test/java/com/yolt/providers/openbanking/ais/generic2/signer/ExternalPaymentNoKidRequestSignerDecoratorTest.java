package com.yolt.providers.openbanking.ais.generic2.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.jwx.Headers;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ExternalPaymentNoKidRequestSignerDecoratorTest {

    private static final String KEY_ID = "KeyId";
    private static final ObjectMapper OBJECT_MAPPER = OpenBankingTestObjectMapper.INSTANCE;
    private static final String ALGORITHM_IDENTIFIER = AlgorithmIdentifiers.RSA_USING_SHA256;

    @Mock
    private DefaultAuthMeans authMeans;

    private ExternalPaymentNoKidRequestSignerDecorator subject = new ExternalPaymentNoKidRequestSignerDecorator(
            OBJECT_MAPPER,
            ALGORITHM_IDENTIFIER,
            new ExternalPaymentNoB64RequestSigner(OBJECT_MAPPER, ALGORITHM_IDENTIFIER));


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