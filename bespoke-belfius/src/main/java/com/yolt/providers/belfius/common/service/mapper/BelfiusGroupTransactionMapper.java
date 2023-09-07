package com.yolt.providers.belfius.common.service.mapper;

import com.yolt.providers.belfius.common.model.ais.Account;
import com.yolt.providers.belfius.common.model.ais.TransactionResponse;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@Service
public class BelfiusGroupTransactionMapper {

    private static final ZoneId BELGIUM_TIMEZONE = ZoneId.of("Europe/Brussels");

    public List<ProviderTransactionDTO> mapTransactions(List<TransactionResponse.Transaction> transactions, Account account) {
        return transactions.stream()
                .map(transaction -> mapTransaction(transaction, account))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO mapTransaction(TransactionResponse.Transaction transaction, Account account) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionRef())
                .dateTime(parseLocalDateToZonedDateTime(transaction.getExecutionDateTime()))
                .type(mapToTransactionType(transaction.getAmount()))
                .category(YoltCategory.GENERAL)
                .amount(transaction.getAmount() != null ? transaction.getAmount().abs() : null)
                .status(TransactionStatus.BOOKED)
                .description(transaction.getRemittanceInfo())
                .extendedTransaction(createExtendedTransaction(transaction, account))
                .build();
    }

    private ZonedDateTime parseLocalDateToZonedDateTime(final String dateToBeParsed) {
        ZonedDateTime hqTime = LocalDate.parse(dateToBeParsed, ISO_LOCAL_DATE).atStartOfDay(ZoneId.of("UTC"));
        return hqTime.withZoneSameInstant(BELGIUM_TIMEZONE);
    }

    private ProviderTransactionType mapToTransactionType(BigDecimal amount) {
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }
        return amount.compareTo(BigDecimal.ZERO) > 0 ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT;
    }

    private ExtendedTransactionDTO createExtendedTransaction(TransactionResponse.Transaction transaction, Account account) {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.BOOKED)
                .bookingDate(parseLocalDateToZonedDateTime(transaction.getExecutionDateTime()))
                .valueDate(transaction.getValueDate() != null ? parseLocalDateToZonedDateTime(transaction.getValueDate()) : null)
                .transactionAmount(new BalanceAmountDTO(toCurrencyCode(transaction.getCurrency()), transaction.getAmount()))
                .creditorName(transaction.getCounterPartyInfo())
                .creditorAccount(retrieveAccountIban(transaction.getCounterPartyAccount()))
                .debtorName(account.getAccountName())
                .debtorAccount(retrieveAccountIban(account.getIban()))
                .remittanceInformationUnstructured(transaction.getRemittanceInfo())
                .build();
    }

    private AccountReferenceDTO retrieveAccountIban(String iban) {
        if (!StringUtils.isEmpty(iban)) {
            return new AccountReferenceDTO(AccountReferenceType.IBAN, iban);
        }
        return null;
    }

    private CurrencyCode toCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (RuntimeException iae) {
            return null;
        }
    }
}
