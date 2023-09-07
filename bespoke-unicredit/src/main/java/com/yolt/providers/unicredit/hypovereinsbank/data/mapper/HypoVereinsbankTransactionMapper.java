package com.yolt.providers.unicredit.hypovereinsbank.data.mapper;

import com.yolt.providers.unicredit.common.data.mapper.CurrencyCodeMapper;
import com.yolt.providers.unicredit.common.data.mapper.TransactionMapper;
import com.yolt.providers.unicredit.common.dto.UniCreditTransactionDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditTransactionsDTO;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class HypoVereinsbankTransactionMapper implements TransactionMapper {

    private final CurrencyCodeMapper currencyCodeMapper;
    private final ZoneId timeZoneId;

    @Override
    public List<ProviderTransactionDTO> mapTransactions(List<UniCreditTransactionsDTO> transactionsDTOs) {
        if (CollectionUtils.isEmpty(transactionsDTOs)) {
            return Collections.emptyList();
        }
        List<ProviderTransactionDTO> providerTransactions = new ArrayList<>();
        for (UniCreditTransactionsDTO transactionsDTO: transactionsDTOs) {
            providerTransactions.addAll(mapTransactionsForStatus(transactionsDTO.getBookedTransactions(), TransactionStatus.BOOKED));
            providerTransactions.addAll(mapTransactionsForStatus(transactionsDTO.getPendingTransactions(), TransactionStatus.PENDING));
        }

        return providerTransactions;
    }

    private List<ProviderTransactionDTO> mapTransactionsForStatus(final List<UniCreditTransactionDTO> transactions, final TransactionStatus status) {
        if (CollectionUtils.isEmpty(transactions)) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .map(it -> mapTransactionToStatus(it, status))
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO mapTransactionToStatus(final UniCreditTransactionDTO transaction, final TransactionStatus status) {
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(!StringUtils.isEmpty(transaction.getBookingDate()) ? parseDate(transaction.getBookingDate()) : null)
                .amount(BigDecimal.valueOf(transaction.getAmount()).abs())
                .type(transaction.getAmount() < 0 ? ProviderTransactionType.DEBIT : ProviderTransactionType.CREDIT)
                .status(status)
                .merchant(transaction.getCreditorName())
                .extendedTransaction(mapExtendedTransaction(transaction, status))
                .description(transaction.getAdditionalInformation())
                .category(YoltCategory.GENERAL)
                .build();
    }

    private ExtendedTransactionDTO mapExtendedTransaction(final UniCreditTransactionDTO transaction, final TransactionStatus status) {
        AccountReferenceDTO creditorAccount = transaction.getCreditorName() == null ? null : new AccountReferenceDTO(AccountReferenceType.IBAN, transaction.getCreditorIban());
        AccountReferenceDTO debtorAccount = transaction.getDebtorName() == null ? null : new AccountReferenceDTO(AccountReferenceType.IBAN, transaction.getDebtorIban());
        return ExtendedTransactionDTO.builder()
                .status(status)
                .entryReference(transaction.getEntryReference())
                .endToEndId(transaction.getEndToEndId())
                .mandateId(transaction.getMandateId())
                .checkId(transaction.getCheckId())
                .creditorId(transaction.getCreditorId())
                .bookingDate(!StringUtils.isEmpty(transaction.getBookingDate()) ? parseDate(transaction.getBookingDate()) : null)
                .valueDate(!StringUtils.isEmpty(transaction.getValueDate()) ? parseDate(transaction.getValueDate()) : null)
                .transactionAmount(new BalanceAmountDTO(currencyCodeMapper.toCurrencyCode(transaction.getCurrency()), BigDecimal.valueOf(transaction.getAmount())))
                .creditorName(transaction.getCreditorName())
                .creditorAccount(creditorAccount)
                .ultimateCreditor(transaction.getUltimateCreditor())
                .debtorName(transaction.getDebtorName())
                .debtorAccount(debtorAccount)
                .ultimateDebtor(transaction.getUltimateDebtor())
                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                .remittanceInformationUnstructured(transaction.getAdditionalInformation())
                .purposeCode(transaction.getPurposeCode())
                .bankTransactionCode(transaction.getBankTransactionCode())
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .transactionIdGenerated(false)
                .build();
    }

    private ZonedDateTime parseDate(String date) {
        return LocalDate.parse(date).atStartOfDay(timeZoneId);
    }
}
