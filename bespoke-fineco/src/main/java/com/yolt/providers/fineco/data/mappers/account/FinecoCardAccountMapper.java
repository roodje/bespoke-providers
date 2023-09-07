package com.yolt.providers.fineco.data.mappers.account;

import com.yolt.providers.fineco.data.mappers.CurrencyCodeMapper;
import com.yolt.providers.fineco.data.mappers.FinecoBalanceMapper;
import com.yolt.providers.fineco.v2.dto.*;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FinecoCardAccountMapper implements FinecoAccountMapper<CardAccountDetails, CardAccountsTransactionsResponse200, ReadCardAccountBalanceResponse200> {

    private static final ZoneId ROME_ZONE_ID = ZoneId.of("Europe/Rome");
    private final FinecoBalanceMapper balanceMapper;
    private final CurrencyCodeMapper currencyCodeMapper;
    private final Clock clock;

    @Override
    public final List<ProviderTransactionDTO> getTransactionList(List<CardAccountsTransactionsResponse200> response) {
        return response.stream()
                .map(CardAccountsTransactionsResponse200::getCardTransactions)
                .map(CardAccountReport::getBooked)
                .flatMap(Collection::stream)
                .map(this::mapToProviderTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public final ProviderAccountNumberDTO getProviderAccountNumberDTO(CardAccountDetails accountDetails) {
        return null;
    }

    @Override
    public final List<Balance> getBalanceList(ReadCardAccountBalanceResponse200 balanceResponse) {
        List<String> incorrectBalanceTypes = new ArrayList();
        List<Balance> correctBalances = new ArrayList();
        balanceResponse.getBalances()
                .forEach(balance -> {
                    if (BalanceType.INTERIMAVAILABLE.equals(balance.getBalanceType())) {
                        correctBalances.add(balance);
                    } else {
                        incorrectBalanceTypes.add(balance.getBalanceType().name());
                    }
                });
        return correctBalances;
    }

    @Override
    public final ProviderAccountDTO getAccount(CardAccountDetails accountDetails,
                                               List<ProviderTransactionDTO> transactions,
                                               List<Balance> balances,
                                               String providerName) {
        BigDecimal currentBalance = balanceMapper.createBalanceForAccount(balances, BalanceType.INTERIMAVAILABLE);
        BigDecimal availableBalance = currentBalance;
        String name = accountDetails.getName();
        if (StringUtils.isEmpty(name)) {
            name = accountDetails.getProduct();
        }
        if (StringUtils.isEmpty(name)) {
            name = providerName;
        }
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CREDIT_CARD)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(availableBalance)
                .currentBalance(currentBalance)
                .accountId(accountDetails.getResourceId())
                .name(name)
                .currency(currencyCodeMapper.toCurrencyCode(accountDetails.getCurrency()))
                .creditCardData(mapToProviderCreditCardDTO(currentBalance))
                .transactions(transactions)
                .extendedAccount(createExtendedAccountDTO(accountDetails, balances))
                .accountMaskedIdentification(accountDetails.getMaskedPan())
                .build();
    }

    private ExtendedAccountDTO createExtendedAccountDTO(final CardAccountDetails account,
                                                        final List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .product(account.getProduct())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .balances(balanceMapper.mapToBalances(balances))
                .name(account.getName())
                .status(Status.fromName(account.getStatus().name()))
                .build();
    }

    private ProviderTransactionDTO mapToProviderTransactionDTO(final CardTransaction transaction) {
        BigDecimal amount = new BigDecimal(transaction.getTransactionAmount().getAmount());
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getCardTransactionId())
                .dateTime(transaction.getBookingDate().atStartOfDay(ROME_ZONE_ID))
                .amount(amount.abs())
                .status(nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED)
                // see https://yolt.atlassian.net/browse/C4PO-3109?focusedCommentId=45986
                // signs are reversed (outgoing/debit = positive, incoming/credit = negative)
                .type(amount.signum() == -1
                        ? ProviderTransactionType.CREDIT
                        : ProviderTransactionType.DEBIT)
                .description(transaction.getTransactionDetails())
                .category(YoltCategory.GENERAL)
                .extendedTransaction(createExtendedTransactionDTO(transaction))
                .build();
    }

    private ExtendedTransactionDTO createExtendedTransactionDTO(final CardTransaction transaction) {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.BOOKED)
                .bookingDate(transaction.getBookingDate().atStartOfDay(ROME_ZONE_ID))
                .valueDate(transaction.getTransactionDate().atStartOfDay(ROME_ZONE_ID))
                .transactionAmount(mapToTransactionAmount(transaction))
                .creditorAccount(transaction.getMaskedPAN() == null ? null : new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, transaction.getMaskedPAN()))
                .transactionIdGenerated(true)
                .build();
    }

    private ProviderCreditCardDTO mapToProviderCreditCardDTO(final BigDecimal availableBalance) {
        return ProviderCreditCardDTO.builder()
                .availableCreditAmount(availableBalance)
                .build();
    }

    private BalanceAmountDTO mapToTransactionAmount(final CardTransaction transaction) {
        return BalanceAmountDTO.builder()
                .currency(CurrencyCode.valueOf(transaction.getTransactionAmount().getCurrency()))
                // see https://yolt.atlassian.net/browse/C4PO-3109?focusedCommentId=45986
                // signs are reversed (outgoing/debit = positive, incoming/credit = negative)
                .amount(new BigDecimal(transaction.getTransactionAmount().getAmount()).negate())
                .build();
    }
}