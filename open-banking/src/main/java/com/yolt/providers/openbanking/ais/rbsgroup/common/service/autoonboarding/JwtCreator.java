package com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding;

import org.jose4j.jwt.JwtClaims;

import java.util.UUID;

public class JwtCreator {

    private static final int JWT_EXPIRATION_TIME_IN_MINUTES = 60;
    private static final String REDIRECT_URIS = "redirect_uris";
    private static final String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";
    private static final String GRANT_TYPES = "grant_types";
    private static final String RESPONSE_TYPES = "response_types";
    private static final String SOFTWARE_ID = "software_id";
    private static final String SCOPE = "scope";
    private static final String SOFTWARE_STATEMENT = "software_statement";
    private static final String APPLICATION_TYPE = "application_type";
    private static final String ID_TOKEN_SIGNED_RESPONSE_ALG = "id_token_signed_response_alg";
    private static final String REQUEST_OBJECT_SIGNING_ALG = "request_object_signing_alg";
    private static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg";


    public JwtClaims prepareJwt(RbsGroupDynamicRegistrationArguments registrationArguments) {
        JwtClaims claims = new JwtClaims();
        String softwareId = registrationArguments.getSoftwareId();
        String institutionId = registrationArguments.getInstitutionId();
        claims.setIssuer(softwareId);
        claims.setAudience(institutionId);
        claims.setClaim(SOFTWARE_ID, softwareId);
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(JWT_EXPIRATION_TIME_IN_MINUTES);
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setClaim(REDIRECT_URIS, registrationArguments.getRedirectUris());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD, registrationArguments.getTokenEndpointAuthMethod());
        claims.setClaim(GRANT_TYPES, registrationArguments.getGrantTypeList());
        claims.setClaim(RESPONSE_TYPES, registrationArguments.getResponseTypesList());
        claims.setClaim(SCOPE, registrationArguments.getScope());
        claims.setClaim(SOFTWARE_STATEMENT, registrationArguments.getSoftwareStatementAssertion());
        claims.setClaim(APPLICATION_TYPE, registrationArguments.getApplicationType());
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG, registrationArguments.getIdTokenSignedResponseAlgorithm());
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG, registrationArguments.getRequestObjectSigningAlgorithm());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_SIGNING_ALG, registrationArguments.getTokenEndpointAuthSigningAlgorithm());
        return claims;
    }
}
