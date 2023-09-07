package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.PermanentTsbGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.auth.PermanentTsbGroupAuthMeansBuilder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermanentTsbGroupPermanentTsbGroupClientAssertionProducerTest {

    private static final String EXPECTED_CLIENT_ASSERTION = "expectedClientAssertion";
    private static final String AUDIENCE = "audience";

    @Mock
    private Signer signer;
    @Mock
    private UserRequestTokenSigner userRequestTokenSigner;

    @Test
    void shouldGenerateClientAssertionForCreateNewClientRequestToken() throws IOException, URISyntaxException, TokenInvalidException, JoseException, MalformedClaimException {
        // given
        PermanentTsbGroupSampleAuthenticationMeans authenticationMeans = new PermanentTsbGroupSampleAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = PermanentTsbGroupAuthMeansBuilder
                .createAuthenticationMeansForAis(authenticationMeans.getPermanentTsbGroupSampleAuthenticationMeansForAis(), "PERMANENT_TSB");

        ArgumentCaptor<JwtClaims> expectedJwtClaimsArgumentCaptor = ArgumentCaptor.forClass(JwtClaims.class);
        when(userRequestTokenSigner.sign(eq(defaultAuthMeans), expectedJwtClaimsArgumentCaptor.capture(), eq(signer))).thenReturn(EXPECTED_CLIENT_ASSERTION);

        PermanentTsbGroupClientAssertionProducer clientAssertionProducer = new PermanentTsbGroupClientAssertionProducer(userRequestTokenSigner, AUDIENCE);

        // when
        String clientAssertion = clientAssertionProducer.createNewClientRequestToken(defaultAuthMeans, signer);

        // then
        assertThat(clientAssertion).isEqualTo(EXPECTED_CLIENT_ASSERTION);
        JwtClaims capturedExpectedJwtClaims = expectedJwtClaimsArgumentCaptor.getValue();
        assertThat(capturedExpectedJwtClaims.getIssuer()).isEqualTo(defaultAuthMeans.getClientId());
        assertThat(capturedExpectedJwtClaims.getSubject()).isEqualTo(defaultAuthMeans.getClientId());
        assertThat(capturedExpectedJwtClaims.getExpirationTime().isAfter(NumericDate.now())).isTrue();
        assertThat(capturedExpectedJwtClaims.getIssuedAt()).isEqualTo(NumericDate.now());
        assertThat(capturedExpectedJwtClaims.getJwtId()).isNotNull();
        assertThat(capturedExpectedJwtClaims.getAudience()).isEqualTo(Collections.singletonList(AUDIENCE));
    }
}
