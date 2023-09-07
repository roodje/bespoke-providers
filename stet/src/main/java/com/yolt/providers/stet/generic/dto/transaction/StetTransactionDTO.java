package com.yolt.providers.stet.generic.dto.transaction;

import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@ProjectedPayload
public interface StetTransactionDTO {

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.entryReference")
    String getEntryReference();

    @JsonPath("$.transactionAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.transactionAmount.currency")
    CurrencyCode getCurrency();

    @JsonPath("$.creditDebitIndicator")
    StetTransactionIndicator getTransactionIndicator();

    @JsonPath("$.status")
    StetTransactionStatus getStatus();

    @JsonPath("$.endToEndId")
    String getEndToEndId();

    @JsonPath("$.bookingDate")
    OffsetDateTime getBookingDate();

    @JsonPath("$.valueDate")
    OffsetDateTime getValueDate();

    @JsonPath("$.transactionDate")
    OffsetDateTime getTransactionDate();

    @JsonPath("$.bankTransactionCode.code")
    String getBankTransactionCode();

    @JsonPath("$.bankTransactionCode.domain")
    String getBankTransactionDomain();

    @JsonPath("$.bankTransactionCode.family")
    String getBankTransactionFamily();

    @JsonPath("$.bankTransactionCode.subFamily")
    String getBankTransactionSubfamily();

    @JsonPath("$.relatedParties.debtor.name")
    String getDebtorName();

    @JsonPath("$.relatedParties.debtorAccount.iban")
    String getDebtorIban();

    @JsonPath("$.relatedParties.ultimateDebtor.name")
    String getUltimateDebtorName();

    @JsonPath("$.relatedParties.creditor.privateId.identification")
    String getCreditorIdentification();

    @JsonPath("$.relatedParties.creditor.name")
    String getCreditorName();

    @JsonPath("$.relatedParties.creditorAccount.iban")
    String getCreditorIban();

    @JsonPath("$.relatedParties.ultimateCreditor.name")
    String getUltimateCreditorName();

    @JsonPath("$.remittanceInformation.unstructured")
    List<String> getUnstructuredRemittanceInformation();

    @JsonPath("$.expectedBookingDate")
    OffsetDateTime getExpectedBookingDate();

    @JsonPath("$.remittanceInformation")
    List<String> getRemittanceInformation();
}
