package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbnAmroPaymentProviderState {

    private String transactionId;
    private String redirectUri;

    private UserAccessTokenState userAccessTokenState;

    @Getter
    @NoArgsConstructor
    public static class UserAccessTokenState {

        private String accessToken;
        private String refreshToken;
        private String expirationTime;

        public UserAccessTokenState(String accessToken,
                                    String refreshToken,
                                    long expiresIn,
                                    Clock clock) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expirationTime = ZonedDateTime.now(clock)
                    .plusSeconds(expiresIn)
                    .format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }

        @JsonIgnore
        public ZonedDateTime getExpirationZonedDateTime() {
            return ZonedDateTime.parse(expirationTime);
        }

    }
}
