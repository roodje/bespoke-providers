package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionIndicator;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import lombok.Builder;
import lombok.Getter;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class TestStetTransactionDTO implements StetTransactionDTO {

    private String resourceId;
    private String entryReference;
    private BigDecimal amount;
    private CurrencyCode currency;
    private StetTransactionIndicator transactionIndicator;
    private StetTransactionStatus status;
    private String endToEndId;
    private OffsetDateTime expectedBookingDate;
    private OffsetDateTime bookingDate;
    private OffsetDateTime valueDate;
    private OffsetDateTime transactionDate;
    private String bankTransactionCode;
    private String bankTransactionDomain;
    private String bankTransactionFamily;
    private String bankTransactionSubfamily;
    private String debtorName;
    private String debtorIban;
    private String ultimateDebtorName;
    private String creditorIdentification;
    private String creditorName;
    private String creditorIban;
    private String ultimateCreditorName;
    private List<String> unstructuredRemittanceInformation;
    private List<String> remittanceInformation;
}
