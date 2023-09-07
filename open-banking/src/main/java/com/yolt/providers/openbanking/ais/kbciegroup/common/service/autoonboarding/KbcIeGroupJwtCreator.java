package com.yolt.providers.openbanking.ais.kbciegroup.common.service.autoonboarding;

import com.yolt.providers.openbanking.ais.kbciegroup.common.model.ContactDto;
import org.jose4j.jwt.JwtClaims;

import java.util.List;
import java.util.UUID;

public class KbcIeGroupJwtCreator implements JwtCreator {

    private static final String REDIRECT_URIS_CLAIM_NAME = "redirect_uris";
    private static final String CLIENT_NAME_CLAIM_NAME = "client_name";
    private static final String CLIENT_DESCRIPTION_CLAIM_NAME = "client_description";
    private static final String JWKS_URI_CLAIN_NAME = "jwks_uri";
    private static final String CONTACTS_CLAIM_NAME = "contacts";
    private static final String ORG_ID_CLAIM_NAME = "org_id";
    private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLAIM_NAME = "token_endpoint_auth_method";
    private static final String GRANT_TYPES_CLAIM_NAME = "grant_types";
    private static final String RESPONSE_TYPES_CLAIM_NAME = "response_types";
    private static final String SCOPE_CLAIM_NAME = "scope";
    private static final String ID_TOKEN_SIGNER_RESPONSE_ALG_CLAIM_NAME = "id_token_signer_response_alg";
    private static final String REQUEST_OBJECT_SIGNING_ALG_NAME = "request_object_signing_alg";
    private static final String SOFTWARE_STATEMENT_CLAIME_NAME = "software_statement";
    private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLAIM_VALUE = "client_secret_post";
    private static final List<String> GRANT_TYPES_CLAIM_VALUE = List.of("client_credentials", "authorization_code", "refresh_token");
    private static final String RESPONSE_TYPES_CLAIM_VALUE = "code";
    public static final String SOFTWARE_ID_CLAIM_NAME = "software_id";
    public static final String APPLICATION_TYPE_CLAIM_NAME = "application_type";
    public static final String APPLICATION_TYPE_CLAIM_VALUE = "web";


    public JwtClaims createSsaClaims(List<String> redirectUris, String clientName, String clientDescription, String jwksUri, String organisationId, String softwareId, List<ContactDto> contactDtoList) {
        JwtClaims claims = new JwtClaims();
        claims.setClaim(REDIRECT_URIS_CLAIM_NAME, redirectUris);
        claims.setClaim(CLIENT_NAME_CLAIM_NAME, clientName);
        claims.setClaim(CLIENT_DESCRIPTION_CLAIM_NAME, clientDescription);
        claims.setClaim(JWKS_URI_CLAIN_NAME, jwksUri);
        claims.setClaim(CONTACTS_CLAIM_NAME, contactDtoList);
        claims.setClaim(ORG_ID_CLAIM_NAME, organisationId);
        claims.setClaim(SOFTWARE_ID_CLAIM_NAME, softwareId);
        return claims;
    }

    public JwtClaims createRegistrationClaims(String issuer, String institutionId, String softwareId, List<String> redirectUris, String scope, String signedSsa, String signingAlgorithm) {
        JwtClaims claims = new JwtClaims();
        claims = new JwtClaims();
        claims.setIssuer(issuer);
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setAudience(institutionId);
        claims.setClaim(REDIRECT_URIS_CLAIM_NAME, redirectUris);
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM_NAME, TOKEN_ENDPOINT_AUTH_METHOD_CLAIM_VALUE);
        claims.setClaim(GRANT_TYPES_CLAIM_NAME, GRANT_TYPES_CLAIM_VALUE);
        claims.setClaim(RESPONSE_TYPES_CLAIM_NAME, RESPONSE_TYPES_CLAIM_VALUE);
        claims.setClaim(SCOPE_CLAIM_NAME, scope);
        claims.setClaim(SOFTWARE_STATEMENT_CLAIME_NAME, signedSsa);
        claims.setClaim(APPLICATION_TYPE_CLAIM_NAME, APPLICATION_TYPE_CLAIM_VALUE);
        claims.setClaim(SOFTWARE_ID_CLAIM_NAME, softwareId);
        claims.setClaim(ID_TOKEN_SIGNER_RESPONSE_ALG_CLAIM_NAME, signingAlgorithm);
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG_NAME, signingAlgorithm);
        return claims;
    }
}
