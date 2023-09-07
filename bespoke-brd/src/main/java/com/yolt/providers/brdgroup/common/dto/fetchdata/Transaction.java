package com.yolt.providers.brdgroup.common.dto.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;

@ProjectedPayload
public interface Transaction {

    @JsonPath("$.endToEndId")
    String getEndToEndId();

    @JsonPath("$.bookingDate")
    String getBookingDate();

    @JsonPath("$.valueDate")
    String getValueDate();

    @JsonPath("$.transactionAmount.currency")
    String getCurrency();

    @JsonPath("$.transactionAmount.amount")
    String getAmount();

    @JsonPath("$.debtorName")
    String getDebtorName();

    @JsonPath("$.debtorAccount.iban")
    String getDebtorIban();

    @JsonPath("$.creditorName")
    String getCreditorName();

    @JsonPath("$.creditorAccount.iban")
    String getCreditorIban();

    @JsonPath("$.remittanceInformationUnstructured")
    String getRemittanceInformationUnstructured();

    default BigDecimal getDecimalAmount() {
        return new BigDecimal(getAmount());
    }
}
