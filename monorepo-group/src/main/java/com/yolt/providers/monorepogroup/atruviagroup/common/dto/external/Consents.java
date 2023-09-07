package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Content of the body of a consent request.
 */
@Builder
public class Consents {

    @JsonProperty("access")
    private AccountAccess access;

    @JsonProperty("recurringIndicator")
    private Boolean recurringIndicator;

    @JsonProperty("validUntil")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntil;

    @JsonProperty("frequencyPerDay")
    private Integer frequencyPerDay;

    @JsonProperty("combinedServiceIndicator")
    private Boolean combinedServiceIndicator;

    /**
     * Requested access services for a consent.
     */
    @Builder
    public static class AccountAccess {

        /**
         * Optional if supported by API provider.  Only the value \"allAccounts\" is admitted.
         */
        public enum AllPsd2Enum {
            ALLACCOUNTS("allAccounts");

            private final String value;

            AllPsd2Enum(String value) {
                this.value = value;
            }

            @JsonValue
            public String getValue() {
                return value;
            }
        }

        @JsonProperty("allPsd2")
        private AllPsd2Enum allPsd2;
    }
}

