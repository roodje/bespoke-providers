package com.yolt.providers.knabgroup.common.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.yolt.providers.common.pis.sepa.SepaPaymentStatus.*;

@AllArgsConstructor
@Getter
public enum SepaTransactionStatus {

    RJCT(REJECTED),
    RCVD(INITIATED),
    ACCP(ACCEPTED),
    CANC(REJECTED),
    ACTC(ACCEPTED);

    private final SepaPaymentStatus sepaPaymentStatus;

    @Override
    @JsonValue
    public String toString() {
        return this.name();
    }

    @JsonCreator
    public static SepaTransactionStatus fromValue(String text) {
        for (SepaTransactionStatus b : SepaTransactionStatus.values()) {
            if (b.name().equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}
