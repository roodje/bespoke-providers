package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
class LclRegistrationUpdateRequest {

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("response_types")
    private List<String> responsesTypes;

    @JsonProperty("contacts")
    private List<String> contacts;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("provider_legal_id")
    private String providerLegalId;

    @JsonProperty("jwks")
    private JsonWebKeySet jwks;

    @JsonProperty("scope")
    private String scope;
}
