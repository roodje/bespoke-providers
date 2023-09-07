package com.yolt.providers.ing.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.yolt.providers.common.pis.sepa.SepaPaymentStatus.*;

@AllArgsConstructor
@Getter
public enum SepaTransactionStatus {

    ACCC(ACCEPTED),
    ACCP(ACCEPTED),
    ACFC(ACCEPTED),
    ACSC(ACCEPTED),
    ACSP(ACCEPTED),
    ACTC(ACCEPTED),
    ACWC(ACCEPTED),
    ACWP(ACCEPTED),
    PART(ACCEPTED),
    PATC(ACCEPTED),
    ACTV(ACCEPTED),
    PDNG(INITIATED),
    RCVD(INITIATED),
    CANC(REJECTED),
    RJCT(REJECTED),
    EXPI(COMPLETED);

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
