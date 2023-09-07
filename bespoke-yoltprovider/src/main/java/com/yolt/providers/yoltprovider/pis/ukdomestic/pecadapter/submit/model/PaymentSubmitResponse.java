package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Objects;
import java.util.stream.Stream;

@Data
public class PaymentSubmitResponse {

    @JsonProperty("Data")
    private Data data;

    @lombok.Data
    public static class Data {

        private String paymentId;
        private String status;

        @JsonCreator
        public Data(@JsonProperty("DomesticPaymentId") String singlePaymentId,
                    @JsonProperty("DomesticScheduledPaymentId") String scheduledPaymentId,
                    @JsonProperty("DomesticStandingOrderId") String periodicPaymentId,
                    @JsonProperty("Status") String status) throws Throwable {
            this.paymentId = Stream.of(singlePaymentId, scheduledPaymentId, periodicPaymentId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            this.status = status;
        }
    }
}