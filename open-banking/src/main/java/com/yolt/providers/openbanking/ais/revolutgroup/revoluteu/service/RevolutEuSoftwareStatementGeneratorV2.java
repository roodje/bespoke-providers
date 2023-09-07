package com.yolt.providers.openbanking.ais.revolutgroup.revoluteu.service;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutEuAuthMeansBuilderV2.*;

public class RevolutEuSoftwareStatementGeneratorV2 {

    private static final String ORG_NAME_CLAIM = "org_name";
    private static final String SOFTWARE_CLIENT_NAME_CLAIM = "software_client_name";
    private static final String ORG_JWKS_ENDPOINT_CLAIM = "org_jwks_endpoint";
    private static final String SOFTWARE_REDIRECT_URIS_CLAIM = "software_redirect_uris";

    public String generateSoftwareStatementClaim(Map<String, BasicAuthenticationMean> authMeans, List<String> redirectUrls) throws JoseException {
        JwtClaims ssaClaims = new JwtClaims();
        ssaClaims.setStringClaim(ORG_NAME_CLAIM, authMeans.get(ORG_NAME_NAME).getValue());
        ssaClaims.setStringClaim(SOFTWARE_CLIENT_NAME_CLAIM, authMeans.get(SOFTWARE_CLIENT_NAME_NAME).getValue());
        ssaClaims.setStringClaim(ORG_JWKS_ENDPOINT_CLAIM, authMeans.get(ORG_JWKS_ENDPOINT_NAME).getValue());
        ssaClaims.setStringListClaim(SOFTWARE_REDIRECT_URIS_CLAIM, redirectUrls);

        JsonWebSignature ssa = new JsonWebSignature();
        ssa.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
        ssa.setAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE);
        ssa.setPayload(ssaClaims.toJson());

        return ssa.getCompactSerialization();
    }
}
