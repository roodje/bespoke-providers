package com.yolt.providers.raiffeisenbank.common.ais.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Getter
@NoArgsConstructor
public class RaiffeisenBankTokens {

    private String accessToken;
    private Long expiryTimestamp;
    private String refreshToken;

    public RaiffeisenBankTokens(final RaiffeisenAuthData authData, Clock clock) {
        this.accessToken = authData.getAccessToken();
        this.refreshToken = authData.getRefreshToken();
        this.expiryTimestamp = createExpiryTimestamp(authData.getExpiresIn(), clock);
    }

    private long createExpiryTimestamp(final Long expiresIn, Clock clock) {
        if (expiresIn == null) {
            return 0;
        }
        return Instant.now(clock).plusSeconds(expiresIn).toEpochMilli();
    }
}
