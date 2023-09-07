package com.yolt.providers.fineco.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.yolt.providers.common.pis.sepa.SepaPaymentStatus.*;

@AllArgsConstructor
@Getter
public enum PaymentStatus {
    ACCC(ACCEPTED),
    ACCP(ACCEPTED),
    ACSC(COMPLETED),
    ACSP(ACCEPTED),
    ACTC(ACCEPTED),
    ACWC(ACCEPTED),
    ACWP(ACCEPTED),
    RCVD(INITIATED),
    PDNG(INITIATED),
    RJCT(REJECTED),
    CANC(REJECTED),
    ACFC(ACCEPTED),
    PATC(ACCEPTED);

    private final SepaPaymentStatus sepaPaymentStatus;

    @Override
    @JsonValue
    public String toString() {
        return this.name();
    }

    @JsonCreator
    public static PaymentStatus fromValue(String text) {
        for (PaymentStatus b : PaymentStatus.values()) {
            if (b.name().equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}