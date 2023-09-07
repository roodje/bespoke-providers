package com.yolt.providers.n26.common.dto.ais.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

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
    String getAmount();

    @JsonPath("$.debtorName")
    String getDebtorName();

    @JsonPath("$.debtorAccount.iban")
    String getDebtorIban();

    @JsonPath("$.creditorName")
    String getCreditorName();

    @JsonPath("$.creditorAccount.iban")
    String getCreditorIban();

    @JsonPath("$.details")
    String getDetails();

    @JsonPath("$.remittanceInformationUnstructuredArray")
    List<String> getRemittanceInformationUnstructuredArray();

    @JsonPath("$.proprietaryBankTransactionCode")
    String getProprietaryBankTransactionCode();

    @JsonPath("$.creditorId")
    String getCreditorId();

    @JsonPath("$.mandateId")
    String getMandateId();

    @JsonPath("$.remittanceInformationUnstructured")
    String getRemittanceInformationUnstructured();

    default BigDecimal getDecimalAmount() {
        return new BigDecimal(getAmount());
    }
}
