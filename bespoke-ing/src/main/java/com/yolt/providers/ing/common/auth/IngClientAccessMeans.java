package com.yolt.providers.ing.common.auth;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

import java.time.Clock;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class IngClientAccessMeans {

    private String accessToken;
    private String tokenType;
    private Long expiryTimestamp;
    private String scope;
    private String clientId;
    private AuthenticationMeansReference authenticationMeansReference;

    public IngClientAccessMeans(final IngAuthData authData, AuthenticationMeansReference authenticationMeansReference, Clock clock) {
        this.accessToken = authData.getAccessToken();
        this.tokenType = authData.getTokenType();
        this.expiryTimestamp = createExpiryTimestamp(authData.getExpiresIn(), clock);
        this.scope = authData.getScope();
        this.clientId = authData.getClientId();
        this.authenticationMeansReference = authenticationMeansReference;
    }

    private long createExpiryTimestamp(final Long expiresIn, Clock clock) {
        if (expiresIn == null) {
            return 0;
        }
        return Instant.now(clock).plusSeconds(expiresIn).getEpochSecond() * 1000;
    }
}