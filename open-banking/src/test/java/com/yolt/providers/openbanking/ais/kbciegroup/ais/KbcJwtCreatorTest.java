package com.yolt.providers.openbanking.ais.kbciegroup.ais;

import com.yolt.providers.openbanking.ais.kbciegroup.common.model.ContactDto;
import com.yolt.providers.openbanking.ais.kbciegroup.common.service.autoonboarding.JwtCreator;
import com.yolt.providers.openbanking.ais.kbciegroup.common.service.autoonboarding.KbcIeGroupJwtCreator;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KbcJwtCreatorTest {

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
    private static final String SOFTWARE_STATEMENT_CLAIM_NAME = "software_statement";
    private static final String SOFTWARE_ID_CLAIM_NAME = "software_id";
    private static final String APPLICATION_TYPE_CLAIM_NAME = "application_type";
    private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLAIM_VALUE = "client_secret_post";
    private static final List<String> GRANT_TYPES_CLAIM_VALUE = List.of("client_credentials", "authorization_code", "refresh_token");
    private static final String RESPONSE_TYPES_CLAIM_VALUE = "code";

    private JwtCreator jwtCreator = new KbcIeGroupJwtCreator();

    @Test
    void shouldReturnSsaClaims() throws MalformedClaimException {
        //given
        List<String> redirectUris = List.of("https://www.redirectUri.com");
        String clientName = "example-client-name";
        String clientDescription = "This is my fancy client desctription with information how awesome is my app!";
        String jwksUri = "https://www.yoltjwks.yolt.com";
        String organizationId = UUID.randomUUID().toString();
        String softwareId = "some-software-id";
        List<ContactDto> contactList = List.of(new ContactDto("name", "email", "phone"));

        //when
        JwtClaims returnedClaims = jwtCreator.createSsaClaims(redirectUris, clientName, clientDescription, jwksUri, organizationId, softwareId, contactList);

        //then
        assertThat(returnedClaims).satisfies(claims -> {
            assertThat(returnedClaims.getClaimValue(REDIRECT_URIS_CLAIM_NAME)).isEqualTo(redirectUris);
            assertThat(returnedClaims.getClaimValue(CONTACTS_CLAIM_NAME)).isEqualTo(contactList);
            assertThat(claims.getClaimValue(CLIENT_NAME_CLAIM_NAME)).isEqualTo(clientName);
            assertThat(claims.getClaimValue(CLIENT_DESCRIPTION_CLAIM_NAME)).isEqualTo(clientDescription);
            assertThat(claims.getClaimValue(JWKS_URI_CLAIN_NAME)).isEqualTo(jwksUri);
            assertThat(claims.getClaimValue(ORG_ID_CLAIM_NAME)).isEqualTo(organizationId);
            assertThat(claims.getClaimValue(SOFTWARE_ID_CLAIM_NAME)).isEqualTo(softwareId);
        });
    }

    @Test
    void shouldReturnClaimsForRegistration() throws MalformedClaimException {
        //given
        String issuer = UUID.randomUUID().toString();
        String institutionId = UUID.randomUUID().toString();
        List<String> redirectUris = List.of("https://www.redirectUri.com");
        String scope = "accounts payments";
        String softwareId = "some-software-id";
        String signedSsa = "someSigned.Ssa.value";
        String algorithm = "RSA256";

        //when
        JwtClaims returnedClaims = jwtCreator.createRegistrationClaims(issuer, institutionId, softwareId, redirectUris, scope, signedSsa, algorithm);

        //then
        assertThat(returnedClaims.getIssuer()).isEqualTo(issuer);
        assertThat(returnedClaims.getIssuedAt()).isNotNull();
        assertThat(returnedClaims.getExpirationTime()).isNotNull();
        assertThat(returnedClaims.getJwtId()).isNotEmpty();
        assertThat(returnedClaims.getAudience().get(0)).isEqualTo(institutionId);
        assertThat(returnedClaims.getClaimValue(REDIRECT_URIS_CLAIM_NAME)).isEqualTo(redirectUris);
        assertThat(returnedClaims.getClaimValue(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM_NAME)).isEqualTo(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM_VALUE);
        assertThat(returnedClaims.getClaimValue(GRANT_TYPES_CLAIM_NAME)).isEqualTo(GRANT_TYPES_CLAIM_VALUE);
        assertThat(returnedClaims.getClaimValue(RESPONSE_TYPES_CLAIM_NAME)).isEqualTo(RESPONSE_TYPES_CLAIM_VALUE);
        assertThat(returnedClaims.getClaimValue(SCOPE_CLAIM_NAME)).isEqualTo(scope);
        assertThat(returnedClaims.getClaimValue(SOFTWARE_STATEMENT_CLAIM_NAME)).isEqualTo(signedSsa);
        assertThat(returnedClaims.getClaimValue(SOFTWARE_ID_CLAIM_NAME)).isEqualTo(softwareId);
        assertThat(returnedClaims.getClaimValue(APPLICATION_TYPE_CLAIM_NAME)).isEqualTo("web");
        assertThat(returnedClaims.getClaimValue(ID_TOKEN_SIGNER_RESPONSE_ALG_CLAIM_NAME)).isEqualTo(algorithm);
        assertThat(returnedClaims.getClaimValue(REQUEST_OBJECT_SIGNING_ALG_NAME)).isEqualTo(algorithm);
    }
}
