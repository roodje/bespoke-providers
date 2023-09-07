package com.yolt.providers.bancatransilvania.common.mapper;

import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.Transaction;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@AllArgsConstructor
public class BancaTransilvaniaGroupTransactionMapper {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public List<ProviderTransactionDTO> mapProviderTransactionsDTO(List<Transaction> transactions, TransactionStatus status) {
        return Optional.ofNullable(transactions)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(transaction -> mapProviderTransactionDTO(transaction, status))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO mapProviderTransactionDTO(Transaction transaction, TransactionStatus status) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(parseToZonedDateTime(status.equals(BOOKED) ? transaction.getBookingDate() : transaction.getValueDate()))
                .type(toProviderTransactionType(transaction))
                .category(YoltCategory.GENERAL)
                .amount(transaction.getDecimalAmount().abs())
                .description(defaultIfEmpty(transaction.getDetails(), transaction.getRemittanceInformationUnstructured()))
                .status(status)
                .extendedTransaction(mapExtendedTransactionDTO(transaction, status))
                .build();
    }

    private static ProviderTransactionType toProviderTransactionType(Transaction transaction) {
        return transaction.getDecimalAmount().compareTo(BigDecimal.ZERO) > 0
                ? ProviderTransactionType.CREDIT : ProviderTransactionType.DEBIT;
    }

    private ExtendedTransactionDTO mapExtendedTransactionDTO(Transaction transaction,
                                                             TransactionStatus status) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .bankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .bookingDate(parseToZonedDateTime(transaction.getBookingDate()))
                .valueDate(parseToZonedDateTime(transaction.getValueDate()))
                .status(status)
                .transactionAmount(mapBalanceAmountDTO(transaction))
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .transactionIdGenerated(true);

        if (Objects.nonNull(transaction.getCreditorName())) {
            builder.creditorName(transaction.getCreditorName())
                    .creditorAccount(mapAccountReferenceDTO(transaction.getCreditorIban()));
        }
        if (Objects.nonNull(transaction.getDebtorName())) {
            builder.debtorName(transaction.getDebtorName())
                    .debtorAccount(mapAccountReferenceDTO(transaction.getDebtorIban()));
        }
        return builder.build();
    }

    @SneakyThrows(ParseException.class)
    public static ZonedDateTime parseToZonedDateTime(String date) {
        if (StringUtils.isNotEmpty(date)) {
            Instant instant = new SimpleDateFormat(DATE_FORMAT).parse(date).toInstant();
            return ZonedDateTime.ofInstant(instant, ZoneId.of("Europe/Bucharest"));
        }
        return null;
    }

    private BalanceAmountDTO mapBalanceAmountDTO(Transaction transaction) {
        return BalanceAmountDTO.builder()
                .amount(transaction.getDecimalAmount())
                .currency(toCurrencyCode(transaction.getCurrency()))
                .build();
    }

    private static CurrencyCode toCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private AccountReferenceDTO mapAccountReferenceDTO(String iban) {
        return AccountReferenceDTO.builder()
                .type(AccountReferenceType.IBAN)
                .value(iban)
                .build();
    }
}
