package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
class JsonWebKeyResponse {

    private String kty;
    private String use;
    private String kid;
    private List<String> x5c;

    @JsonProperty("key_ops")
    private List<String> keysOps;

    @JsonProperty("x5t#S256")
    private String x5ts256;
}
