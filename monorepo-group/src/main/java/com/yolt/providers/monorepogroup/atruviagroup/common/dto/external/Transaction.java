package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;

@ProjectedPayload
public interface Transaction {

    @JsonPath("$.transactionId")
    String getTransactionId();

    @JsonPath("$.bookingDate")
    String getBookingDate();

    @JsonPath("$.valueDate")
    String getValueDate();

    @JsonPath("$.transactionAmount.currency")
    String getCurrency();

    @JsonPath("$.transactionAmount.amount")
    BigDecimal getAmount();

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
}
