package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Content of the body of a Update PSU Authentication Request.   Password subfield is used.
 */
@Data
public class UpdatePsuAuthentication {

    @JsonProperty("psuData")
    private PsuData psuData;

    @Data
    public static class PsuData {

        @JsonProperty("password")
        private String password;

        public PsuData password(String password) {
            this.password = password;
            return this;
        }
    }
}

