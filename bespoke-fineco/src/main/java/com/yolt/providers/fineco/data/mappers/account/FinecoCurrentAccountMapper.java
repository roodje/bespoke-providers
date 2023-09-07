package com.yolt.providers.fineco.data.mappers.account;

import com.yolt.providers.fineco.data.mappers.CurrencyCodeMapper;
import com.yolt.providers.fineco.data.mappers.FinecoBalanceMapper;
import com.yolt.providers.fineco.v2.dto.*;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
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
import java.util.*;
import java.util.stream.Collectors;

import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;

@AllArgsConstructor
public final class FinecoCurrentAccountMapper implements FinecoAccountMapper<AccountDetails, TransactionsResponse200, ReadAccountBalanceResponse200> {

    private static final ZoneId ROME_ZONE_ID = ZoneId.of("Europe/Rome");
    private final FinecoBalanceMapper balanceMapper;
    private final CurrencyCodeMapper currencyCodeMapper;
    private final Clock clock;

    private final Set<BalanceType> supportedBalanceTypes = new HashSet<>(Arrays.asList(
            BalanceType.CLOSINGBOOKED,
            BalanceType.EXPECTED,
            BalanceType.OPENINGBOOKED,
            BalanceType.INTERIMAVAILABLE,
            BalanceType.AUTHORISED,
            BalanceType.INTERIMBOOKED,
            BalanceType.FORWARDAVAILABLE
    ));

    @Override
    public final ProviderAccountNumberDTO getProviderAccountNumberDTO(AccountDetails accountDetails) {
        return new ProviderAccountNumberDTO(IBAN, accountDetails.getIban());
    }

    @Override
    public final List<ProviderTransactionDTO> getTransactionList(List<TransactionsResponse200> transactionResponse) {
        return transactionResponse.stream()
                .map(TransactionsResponse200::getTransactions)
                .map(AccountReport::getBooked)
                .flatMap(Collection::stream)
                .map(this::mapToProviderTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public final List<Balance> getBalanceList(ReadAccountBalanceResponse200 balanceResponse) {
        List<String> incorrectBalanceTypes = new ArrayList();
        List<Balance> correctBalances = new ArrayList();

        balanceResponse.getBalances()
                .forEach(balance -> {
                    if (supportedBalanceTypes.contains(balance.getBalanceType())) {
                        correctBalances.add(balance);
                    } else {
                        incorrectBalanceTypes.add(balance.getBalanceType().name());
                    }
                });
        return correctBalances;
    }

    @Override
    public final ProviderAccountDTO getAccount(AccountDetails accountDetails,
                                               List<ProviderTransactionDTO> transactions,
                                               List<Balance> balances,
                                               String providerName) {
        BigDecimal currentBalance = balanceMapper.createBalanceForAccount(balances, BalanceType.INTERIMBOOKED);
        BigDecimal availableBalance = balanceMapper.createBalanceForAccount(balances, BalanceType.INTERIMAVAILABLE);

        String name = accountDetails.getProduct();
        if (StringUtils.isEmpty(name)) {
            name = providerName;
        }

        ProviderAccountNumberDTO accountNumberDTO = new ProviderAccountNumberDTO(IBAN, accountDetails.getIban());
        accountNumberDTO.setHolderName(accountDetails.getOwnerName());
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(availableBalance)
                .currentBalance(currentBalance)
                .accountId(accountDetails.getResourceId())
                .accountNumber(accountNumberDTO)
                .name(name)
                .currency(currencyCodeMapper.toCurrencyCode(accountDetails.getCurrency()))
                .transactions(transactions)
                .extendedAccount(createExtendedAccountDTO(accountDetails, balances))
                .build();
    }

    private ExtendedAccountDTO createExtendedAccountDTO(final AccountDetails account,
                                                        final List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .product(account.getProduct())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .balances(balanceMapper.mapToBalances(balances))
                .build();
    }

    private ProviderTransactionDTO mapToProviderTransactionDTO(final TransactionDetails transaction) {
        BigDecimal amount = new BigDecimal(transaction.getTransactionAmount().getAmount());
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(transaction.getBookingDate().atStartOfDay(ROME_ZONE_ID))
                .amount(amount.abs())
                .status(nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED)
                .type(amount.signum() == 1
                        ? ProviderTransactionType.CREDIT
                        : ProviderTransactionType.DEBIT)
                .description(transaction.getRemittanceInformationUnstructured())
                .category(YoltCategory.GENERAL)
                .merchant(transaction.getCreditorName())
                .extendedTransaction(createExtendedTransactionDTO(transaction))
                .build();
    }

    private ExtendedTransactionDTO createExtendedTransactionDTO(final TransactionDetails transaction) {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.BOOKED)
                .mandateId(transaction.getMandateId())
                .checkId(transaction.getCheckId())
                .creditorId(transaction.getCreditorId())
                .bookingDate(transaction.getBookingDate().atStartOfDay(ROME_ZONE_ID))
                .valueDate(transaction.getValueDate().atStartOfDay(ROME_ZONE_ID))
                .transactionAmount(mapToTransactionAmount(transaction))
                .creditorName(transaction.getCreditorName())
                .creditorAccount(transaction.getCreditorAccount() == null ? null : mapAccountReferenceToAccountReferenceDTO(transaction.getCreditorAccount()))
                .ultimateCreditor(transaction.getUltimateCreditor())
                .debtorName(transaction.getDebtorName())
                .ultimateDebtor(transaction.getUltimateDebtor())
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .bankTransactionCode(transaction.getBankTransactionCode())
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode())
                .transactionIdGenerated(true)
                .build();
    }

    private AccountReferenceDTO mapAccountReferenceToAccountReferenceDTO(AccountReference accountReference) {
        EnumMap<AccountReferenceType, String> map = new EnumMap<>(AccountReferenceType.class);
        map.put(AccountReferenceType.IBAN, accountReference.getIban());
        map.put(AccountReferenceType.BBAN, accountReference.getBban());
        map.put(AccountReferenceType.PAN, accountReference.getPan());
        map.put(AccountReferenceType.MASKED_PAN, accountReference.getMaskedPan());
        Map.Entry<AccountReferenceType, String> entryValue = map.entrySet()
                .stream()
                .filter(entry -> StringUtils.isNotEmpty(entry.getValue()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Fineco: Unsupported account type."));

        return new AccountReferenceDTO(entryValue.getKey(), entryValue.getValue());
    }

    private static BalanceAmountDTO mapToTransactionAmount(final TransactionDetails transaction) {
        return BalanceAmountDTO.builder()
                .currency(CurrencyCode.valueOf(transaction.getTransactionAmount().getCurrency()))
                .amount(new BigDecimal(transaction.getTransactionAmount().getAmount()))
                .build();
    }
}
