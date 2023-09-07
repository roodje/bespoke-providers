package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
class JsonWebKeySetResponse {

    @JsonProperty("keys")
    private List<JsonWebKeyResponse> keys = new ArrayList<>();
}
