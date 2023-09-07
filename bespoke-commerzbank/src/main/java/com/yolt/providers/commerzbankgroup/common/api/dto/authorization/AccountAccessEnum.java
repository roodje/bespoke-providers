package com.yolt.providers.commerzbankgroup.common.api.dto.authorization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class AccountAccessEnum {

    @JsonProperty("allPsd2")
    private AllPsd2Enum allPsd2 = null;

    public enum AllPsd2Enum {
        ALLACCOUNTS("allAccounts"),
        ALLACCOUNTSWITHOWNERNAME("allAccountsWithOwnerName");

        private final String value;

        AllPsd2Enum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static AllPsd2Enum fromValue(String text) {
            for (AllPsd2Enum b : AllPsd2Enum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + text + "'");
        }
    }

    public AccountAccessEnum allPsd2(AllPsd2Enum allPsd2) {
        this.allPsd2 = allPsd2;
        return this;
    }
}
