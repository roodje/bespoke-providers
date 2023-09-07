package com.yolt.providers.stet.cicgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonPropertyOrder({
        "client_id",
        "registration_client_uri",
        "redirect_uris",
        "token_endpoint_auth_method",
        "tls_client_auth_subject_dn",
        "grant_types",
        "response_types",
        "client_name",
        "logo_uri",
        "scope",
        "contacts",
        "jwks",
        "provider_legal_id"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CicGroupRegistrationRequestDTO {

    @JsonProperty("client_id")
    private String clientId;

    public static final String JSON_PROPERTY_REGISTRATION_CLIENT_URI = "registration_client_uri";
    @JsonProperty("registration_client_uri")
    private String registrationClientUri;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    @JsonProperty("tls_client_auth_subject_dn")
    private String tlsClientAuthSubjectDn;

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("response_types")
    private List<String> responseTypes;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("logo_uri")
    private String logoUri;

    private List<String> contacts;
    private String scope;
    private CicGroupJsonWebKeySet jwks;

    @JsonProperty("provider_legal_id")
    private String providerLegalId;
}
