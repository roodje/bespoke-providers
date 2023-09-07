package com.yolt.providers.bunq.common.auth;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.security.KeyPair;

@Data
public class BunqApiContext {
    @NotEmpty
    private final String bunqUserId;
    @NotEmpty
    private final String serverToken;
    @NotEmpty
    private final KeyPair keyPair;
    @NotEmpty
    private final String oauthToken;
    @NotEmpty
    private final String sessionToken;
    @NotEmpty
    private final Long expiryTimeInSeconds;
}
