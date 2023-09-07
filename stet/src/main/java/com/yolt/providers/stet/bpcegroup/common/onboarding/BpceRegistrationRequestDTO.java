package com.yolt.providers.stet.bpcegroup.common.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
class BpceRegistrationRequestDTO {

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("response_types")
    private List<String> responsesTypes;

    @JsonProperty("contact")
    private Contact contact;

    @JsonProperty("provider_legal_id")
    private String providerLegalId;

    @JsonProperty("jwks")
    private JsonWebKeySet jwks;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("software_id")
    private String softwareId;

    @Getter
    @Setter
    static class Contact {

        @JsonProperty("contact_name")
        private String contactName;
        @JsonProperty("email")
        private String email;
        @JsonProperty("phone_number")
        private String phoneNumber;
    }

    @Getter
    @Setter
    static class JsonWebKeySet {

        @JsonProperty("keys")
        private List<JsonWebKey> keys = new ArrayList<>();

        public JsonWebKeySet addWebKey(JsonWebKey webKey) {
            keys.add(webKey);
            return this;
        }

        @Getter
        @Setter
        @Builder
        static
        class JsonWebKey {

            private String kty;
            private String use;
            private String alg;
            @JsonProperty("keys_ops")
            private List<String> keysOps;
            private String kid;
            private List<String> x5c;
            @JsonProperty("x5t#S256")
            private String x5ts256;
        }
    }
}
