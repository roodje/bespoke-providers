package com.yolt.providers.openbanking.ais.cybgroup.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CybgGroupClientRegistration {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("logo_uri")
    private String logoUri;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("id_token_signed_response_alg")
    private String idTokenSignedResponseAlg;

    @JsonProperty("request_object_signing_alg")
    private String requestObjectSigningAlg;

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("software_id")
    private String softwareId;

    @JsonProperty("software_logo_uri")
    private String softwareLogoUri;

    @JsonProperty("software_client_name")
    private String softwareClientName;

    @JsonProperty("software_roles")
    private List<String> softwareRoles;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    @JsonProperty("software_jwks_endpoint")
    private String softwareJwksEndpoint;

    @JsonProperty("client_secret")
    private String clientSecret;
}
