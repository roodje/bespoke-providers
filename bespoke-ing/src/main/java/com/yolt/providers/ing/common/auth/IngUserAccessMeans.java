package com.yolt.providers.ing.common.auth;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class IngUserAccessMeans {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiryTimestamp;
    private Long refreshTokenExpiryTimestamp;
    private String scope;
    private IngClientAccessMeans clientAccessMeans;

    public IngUserAccessMeans(final IngAuthData authData, final IngClientAccessMeans clientAccessMeans, Clock clock) {
        this.accessToken = authData.getAccessToken();
        this.refreshToken = authData.getRefreshToken();
        this.tokenType = authData.getTokenType();
        this.expiryTimestamp = createExpiryTimestamp(authData.getExpiresIn(), clock);
        this.refreshTokenExpiryTimestamp = createExpiryTimestamp(authData.getRefreshTokenExpiresIn(), clock);
        this.scope = authData.getScope();
        this.clientAccessMeans = clientAccessMeans;

    }

    public IngUserAccessMeans update(final IngUserAccessMeans newUserAccessMeans, Clock clock) {
        IngUserAccessMeans updatedUserAccessMeans = new IngUserAccessMeans();
        updatedUserAccessMeans.accessToken = newUserAccessMeans.getAccessToken();
        updatedUserAccessMeans.expiryTimestamp = createExpiryTimestamp(newUserAccessMeans.getExpiryTimestamp(), clock);
        updatedUserAccessMeans.scope = newUserAccessMeans.getScope();
        updatedUserAccessMeans.tokenType = newUserAccessMeans.getTokenType();
        updatedUserAccessMeans.refreshToken = this.refreshToken;
        updatedUserAccessMeans.refreshTokenExpiryTimestamp = this.refreshTokenExpiryTimestamp;
        updatedUserAccessMeans.clientAccessMeans = this.clientAccessMeans;
        return updatedUserAccessMeans;
    }

    private long createExpiryTimestamp(final Long expiresIn, final Clock clock) {
        if (expiresIn == null) {
            return 0;
        }
        return Instant.now(clock).plusSeconds(expiresIn).getEpochSecond() * 1000;
    }
}