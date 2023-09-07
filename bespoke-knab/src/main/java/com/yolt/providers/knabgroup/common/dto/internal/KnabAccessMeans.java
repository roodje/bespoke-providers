package com.yolt.providers.knabgroup.common.dto.internal;

import com.yolt.providers.knabgroup.common.dto.external.AuthData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class KnabAccessMeans {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiryTimestamp;
    private String scope;

    public KnabAccessMeans(final AuthData authData, final Clock clock) {
        this.accessToken = authData.getAccessToken();
        this.refreshToken = authData.getRefreshToken();
        this.tokenType = authData.getTokenType();
        this.expiryTimestamp = createExpiryTimestamp(authData.getExpiresIn(), clock);
        this.scope = authData.getScope();
    }

    private long createExpiryTimestamp(final Long expiresIn, final Clock clock) {
        if (expiresIn == null) {
            return 0;
        }
        return Instant.now(clock).plusSeconds(expiresIn).getEpochSecond() * 1000;
    }
}