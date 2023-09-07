package com.yolt.providers.axabanque.common.pkce;

import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PKCETest {
    private static final byte[] RFC_RANDOM_BYTES = new byte[]{(byte) 116, (byte) 24, (byte) 223, (byte) 180, (byte) 151,
            (byte) 153, (byte) 224, (byte) 37, (byte) 79, (byte) 250, (byte) 96, (byte) 125, (byte) 216, (byte) 173,
            (byte) 187, (byte) 186, (byte) 22, (byte) 212, (byte) 37, (byte) 77, (byte) 105, (byte) 214, (byte) 191,
            (byte) 240, (byte) 91, (byte) 88, (byte) 5, (byte) 88, (byte) 83, (byte) 132, (byte) 141, (byte) 121};

    @Test
    public void shouldGeneratePKCEMeansForCreateRandomS256WithRandomBytes() {
        //given
        PKCE pkce = new PKCE();

        // when
        OAuth2ProofKeyCodeExchange oAuth2ProofKeyCodeExchange = pkce.createRandomS256(RFC_RANDOM_BYTES);

        // then
        assertThat(oAuth2ProofKeyCodeExchange.getCodeVerifier()).isEqualTo("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");
        assertThat(oAuth2ProofKeyCodeExchange.getCodeChallenge()).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }
}
