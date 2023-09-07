package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

/**
 * It is contained in addition to the data element &#39;chosenScaMethod&#39; if challenge data is needed for SCA. In rare cases this attribute is also used in the context of the &#39;startAuthorisationWithPsuAuthentication&#39; link.
 */
@ProjectedPayload
public interface ChallengeData {

    @JsonPath("$.image")
    byte[] getImage();

    @JsonPath("$.data")
    List<String> getData();

    @JsonPath("$.imageLink")
    String getImageLink();

    @JsonPath("$.otpMaxLength")
    Integer getOtpMaxLength();

    /**
     * The format type of the OTP to be typed in. The admitted values are \"characters\" or \"integer\".
     */
    enum OtpFormatEnum {
        CHARACTERS("characters"),
        INTEGER("integer");

        private final String value;

        OtpFormatEnum(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static OtpFormatEnum fromValue(String value) {
            for (OtpFormatEnum b : OtpFormatEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    @JsonPath("$.otpFormat")
    OtpFormatEnum getOtpFormat();

    @JsonPath("$.additionalInformation")
    String getAdditionalInformation();
}

