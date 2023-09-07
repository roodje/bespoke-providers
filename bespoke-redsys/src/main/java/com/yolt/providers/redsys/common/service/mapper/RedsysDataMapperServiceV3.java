package com.yolt.providers.redsys.common.service.mapper;

import com.yolt.providers.redsys.common.dto.AccountDetails;
import com.yolt.providers.redsys.common.dto.Balance;
import com.yolt.providers.redsys.common.dto.Transaction;
import com.yolt.providers.redsys.common.util.BalanceType;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static com.yolt.providers.redsys.common.util.BalanceType.*;

@AllArgsConstructor
public class RedsysDataMapperServiceV3 implements RedsysDataMapperService {

    private final CurrencyCodeMapper currencyCodeMapper;
    private final RedsysExtendedDataMapperV2 extendedDataMapper;
    private final Clock clock;

    private final String transactionDescriptionFallback;

    @Override
    public ProviderAccountDTO toProviderAccountDTO(final AccountDetails account,
                                                   final List<Balance> accountBalances,
                                                   final List<ProviderTransactionDTO> transactionsConverted,
                                                   final String providerName) {
        String name;
        if (StringUtils.isNotBlank(account.getName())) {
            name = account.getName();
        } else if (StringUtils.isNotBlank(account.getProduct())) {
            name = account.getProduct();
        } else {
            name = providerName;
        }

        final BigDecimal availableBalance = findBalance(accountBalances, getPreferredBalanceTypesForAvailableBalance());
        final BigDecimal currentBalance = findBalance(accountBalances, getPreferredBalanceTypesForCurrentBalance());

        return ProviderAccountDTO.builder()
                .yoltAccountType(toAccountType(account))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(availableBalance)
                .currentBalance(currentBalance)
                .accountId(account.getResourceId())
                .accountNumber(getAccountNumber(account))
                .name(name)
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .bic(account.getBic())
                .transactions(transactionsConverted)
                .extendedAccount(extendedDataMapper.createExtendedAccountDTO(account, accountBalances, name))
                .build();
    }

    private AccountType toAccountType(final AccountDetails account) {
        if ("CARD".equals(account.getCashAccountType())) {
            return AccountType.CREDIT_CARD;
        }
        return AccountType.CURRENT_ACCOUNT;
    }

    private List<BalanceType> getPreferredBalanceTypesForCurrentBalance() {
        return Arrays.asList(CLOSING_BOOKED, INTERIM_BOOKED, INTERIM_AVAILABLE, OPENING_BOOKED, EXPECTED, FORWARD_AVAILABLE);
    }

    private List<BalanceType> getPreferredBalanceTypesForAvailableBalance() {
        return Arrays.asList(INTERIM_AVAILABLE, INTERIM_BOOKED, OPENING_BOOKED, CLOSING_BOOKED, EXPECTED, FORWARD_AVAILABLE);
    }

    private ProviderAccountNumberDTO getAccountNumber(final AccountDetails account) {
        ProviderAccountNumberDTO providerAccountNumberDTO = null;

        if (StringUtils.isNotBlank(account.getIban())) {
            providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
            providerAccountNumberDTO.setHolderName(account.getOwnerName());
        }
        return providerAccountNumberDTO;
    }

    private BigDecimal findBalance(final List<Balance> accountBalances,
                                   final List<BalanceType> preferredBalances) {
        for (BalanceType preferredBalance : preferredBalances) {
            BigDecimal balanceAmount = findBalanceForPreferredBalance(accountBalances, preferredBalance.getBalanceType());
            if (balanceAmount != null) {
                return balanceAmount;
            }
        }
        return null;
    }

    private BigDecimal findBalanceForPreferredBalance(final List<Balance> accountBalances, final String preferredBalance) {
        return accountBalances.stream()
                .filter(balance -> preferredBalance.equalsIgnoreCase(balance.getBalanceType()))
                .findFirst()
                .map(balance -> new BigDecimal(balance.getBalanceAmount().getAmount().replace(",", ".")))
                .orElse(null);
    }

    @Override
    public ProviderTransactionDTO toProviderTransactionDTO(final Transaction transaction, TransactionStatus transactionStatus) {
        final BigDecimal amount = new BigDecimal(transaction.getTransactionAmount().getAmount());
        ExtendedTransactionDTO extendedTransaction = extendedDataMapper.toExtendedTransactionDTO(transaction, transactionStatus);
        String description = ObjectUtils.firstNonNull(extendedTransaction.getRemittanceInformationUnstructured(),
                extendedTransaction.getRemittanceInformationStructured(), transactionDescriptionFallback);
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(extendedDataMapper.parseLocalDateWithAddedZonedTime(transaction.getBookingDate()))
                .type(toProviderTransactionType(amount))
                .category(YoltCategory.GENERAL)
                .amount(amount.abs())
                .description(description)
                .status(transactionStatus)
                .extendedTransaction(extendedTransaction)
                .build();
    }

    protected ProviderTransactionType toProviderTransactionType(final BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0
                ? ProviderTransactionType.CREDIT
                : ProviderTransactionType.DEBIT;
    }
}
