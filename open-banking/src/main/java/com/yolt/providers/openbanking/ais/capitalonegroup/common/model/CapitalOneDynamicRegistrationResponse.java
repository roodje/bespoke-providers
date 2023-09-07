package com.yolt.providers.openbanking.ais.capitalonegroup.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CapitalOneDynamicRegistrationResponse {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("registration_access_token")
    private String registrationAccessToken;

    @JsonProperty("registration_client_uri")
    private String registrationClientUri;

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("jwks_uri")
    private String jwksUri;

    @JsonProperty("response_types")
    private List<String> responseTypes;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    @JsonProperty("request_object_signing_alg")
    private String requestObjectSigningAlg;

    @JsonProperty("token_endpoint_auth_signing_alg")
    private String tokenEndpointAuthSigningAlg;

    @JsonProperty("id_token_signed_response_alg")
    private String idTokenSignedResponseAlg;

    @JsonProperty("software_on_behalf_of")
    private String softwareOnBehalfOf;

    @JsonProperty("org_id")
    private String orgId;

    @JsonProperty("org_name")
    private String orgName;
}
