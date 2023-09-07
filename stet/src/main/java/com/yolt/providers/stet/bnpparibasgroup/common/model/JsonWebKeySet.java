package com.yolt.providers.stet.bnpparibasgroup.common.model;

import com.yolt.providers.stet.bnpparibasgroup.common.onboarding.JsonWebKey;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * An RFC 7517 Json Web Key Set
 * Example: https://auth0.com/docs/tokens/reference/jwt/jwks-properties
 */
@Getter
@Setter
public class JsonWebKeySet {

    private List<JsonWebKey> keys = new ArrayList<>();

    public void addWebKey(JsonWebKey webKey) {
        keys.add(webKey);
    }
}
