package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Getter
@NoArgsConstructor
public class BankVanBredaGroupTokens {

    private String accessToken;
    private Long expiryTimestamp;
    private String refreshToken;

    public BankVanBredaGroupTokens(final BankVanBredaGroupAuthData authData, Clock clock) {
        this.accessToken = authData.getAccessToken();
        this.refreshToken = authData.getRefreshToken();
        this.expiryTimestamp = createExpiryTimestamp(authData.getExpiresIn(), clock);
    }

    public BankVanBredaGroupTokens(final BankVanBredaGroupAuthData authData, Clock clock, String refreshToken) {
        this(authData, clock);
        this.refreshToken = refreshToken;
    }

    private long createExpiryTimestamp(final Long expiresIn, Clock clock) {
        if (expiresIn == null) {
            return 0;
        }
        return Instant.now(clock).plusSeconds(expiresIn).toEpochMilli();
    }
}
