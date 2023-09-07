package com.yolt.providers.openbanking.ais.barclaysgroup.pis;

import com.yolt.providers.openbanking.ais.barclaysgroup.common.signer.BarclaysGroupPaymentRequestSignerV3;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

/**
 * This test contains all possible flows regarding signature header creation logic in Barclays.
 * <p>
 * Covered flows:
 * - removing b64 `false` value claim from Json Web Signature
 * - throwing exception when b64 `true` value claim is in Json Web Signature
 * - throwing exception when b64 claim is missing in Json Web Signature
 * - not removing b64 `false` value claim from Json Web Signature when keyId is header is set
 * <p>
 */
class BarclaysGroupPaymentRequestSignerV3Test {

    private static final String KEY_ID = "KeyId";
    private final DefaultAuthMeans defaultAuthMeans = mock(DefaultAuthMeans.class);
    private BarclaysGroupPaymentRequestSignerV3 subject;

    @BeforeEach
    public void setUp() {
        subject = new BarclaysGroupPaymentRequestSignerV3(null, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
    }

    @Test
    public void shouldNotContainB64InJws() {
        // given
        JsonWebSignature jws = new JsonWebSignature();
        jws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, false);
        jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);
        jws.setKeyIdHeaderValue(null);

        // when
        subject.adjustJWSHook(jws, defaultAuthMeans);

        // then
        String headersAsString = jws.getHeaders().getFullHeaderAsJsonString();
        assertThat(headersAsString).doesNotContain("b64");
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
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> subject.adjustJWSHook(jws, defaultAuthMeans))
                .withMessage("Wrong format of Barclays payment claims: adjusted headers should contain b64 headers with value false");
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenB64IsMissing() {
        // given
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);

        // when -> then
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> subject.adjustJWSHook(jws, defaultAuthMeans))
                .withMessage("Wrong format of Barclays payment claims: adjusted headers should contain b64 headers with value false");
    }

    @Test
    public void shouldNotRemoveKeyId() {
        // given
        JsonWebSignature jws = new JsonWebSignature();
        jws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, false);
        jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD);
        jws.setKeyIdHeaderValue(KEY_ID);

        // when
        subject.adjustJWSHook(jws, defaultAuthMeans);

        // then
        assertThat(jws.getKeyIdHeaderValue()).isEqualTo(KEY_ID);
    }
}