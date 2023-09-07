package com.yolt.providers.openbanking.ais.revolutgroup.revoluteu.service;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.revolutgroup.RevolutSampleAuthenticationMeans;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains tests for software statement claim required during Revolut Eu autoonboarding process.
 * <p>
 * Covered flows:
 * - generation of software statement claim
 * <p>
 */
public class RevolutEuSoftwareStatementGeneratorV2Test {

    private static final String ORG_NAME_CLAIM = "org_name";
    private static final String SOFTWARE_CLIENT_NAME_CLAIM = "software_client_name";
    private static final String ORG_JWKS_ENDPOINT_CLAIM = "org_jwks_endpoint";
    private static final String SOFTWARE_REDIRECT_URIS_CLAIM = "software_redirect_uris";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback";

    private RevolutEuSoftwareStatementGeneratorV2 softwareStatementGenerator = new RevolutEuSoftwareStatementGeneratorV2();

    @Test
    public void shouldGenerateSoftwareStatementClaimWithProperFields() throws Exception {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new RevolutSampleAuthenticationMeans().getAuthenticationMeansForEidasRegistration();

        // when
        String softwareStatementClaim = softwareStatementGenerator.generateSoftwareStatementClaim(authenticationMeans, Collections.singletonList(REDIRECT_URL));

        // then
        assertThat(softwareStatementClaim).isEqualTo("eyJhbGciOiJub25lIn0.eyJvcmdfbmFtZSI6IlRQUCBPcmcgTmFtZSIsInNvZnR3YXJlX2NsaWVudF9uYW1lIjoiVFBQIE5hbWUiLCJvcmdfandrc19lbmRwb2ludCI6Imh0dHBzOi8va2V5c3RvcmUub3BlbmJhbmtpbmcub3JnLnVrL29yZ2FuaXphdGlvbklkL3NvZnR3YXJlSWQuandrcyIsInNvZnR3YXJlX3JlZGlyZWN0X3VyaXMiOlsiaHR0cHM6Ly93d3cueW9sdC5jb20vY2FsbGJhY2siXX0.");
        final JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(softwareStatementClaim);
        JwtClaims claims = JwtClaims.parse(jws.getUnverifiedPayload());
        assertThat(claims.getClaimsMap()).hasSize(4);
        assertThat(claims.getClaimsMap()).containsOnlyKeys(
                ORG_NAME_CLAIM,
                SOFTWARE_CLIENT_NAME_CLAIM,
                ORG_JWKS_ENDPOINT_CLAIM,
                SOFTWARE_REDIRECT_URIS_CLAIM
        );
        assertThat(jws.getAlgorithmNoConstraintCheck().getAlgorithmIdentifier()).isEqualTo(AlgorithmIdentifiers.NONE);
    }
}
