package com.yolt.providers.openbanking.ais.generic2.pec.status.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Data
public class ScheduledPaymentStatusResponse {

    @JsonProperty("Data")
    private Data data;

    @lombok.Data
    public static class Data {

        private String resourceId;
        private Status status;

        @JsonCreator
        public Data(@JsonProperty("ConsentId") String consentId,
                    @JsonProperty("DomesticScheduledPaymentId") String domesticPaymentId,
                    @JsonProperty("Status") Status status) {
            this.status = status;
            this.resourceId = StringUtils.isEmpty(domesticPaymentId) ? consentId : domesticPaymentId;
        }

        @RequiredArgsConstructor
        public enum Status {
            AUTHORISED("Authorised"),
            AWAITINGAUTHORISATION("AwaitingAuthorisation"),
            CONSUMED("Consumed"),
            REJECTED("Rejected"),
            CANCELLED("Cancelled"),
            INITIATIONCOMPLETED("InitiationCompleted"),
            INITIATIONFAILED("InitiationFailed"),
            INITIATIONPENDING("InitiationPending");


            private final String value;

            @Override
            @JsonValue
            public String toString() {
                return String.valueOf(value);
            }

            @JsonCreator
            public static Status fromValue(String value) {
                return Arrays.stream(Status.values())
                        .filter(status -> status.value.equalsIgnoreCase(value))
                        .findFirst()
                        .orElse(null);
            }
        }
    }
}