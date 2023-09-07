package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
class JsonWebKeySet {

    @JsonProperty("keys")
    private List<JsonWebKey> keys = new ArrayList<>();

    public JsonWebKeySet addWebKey(JsonWebKey webKey) {
        keys.add(webKey);
        return this;
    }
}
