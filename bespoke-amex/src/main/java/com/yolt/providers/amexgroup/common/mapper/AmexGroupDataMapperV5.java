package com.yolt.providers.amexgroup.common.mapper;

import com.yolt.providers.amex.common.dto.*;
import com.yolt.providers.amexgroup.common.utils.AmexDateTimeUtils;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExchangeRateDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.*;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.*;

import static nl.ing.lovebird.extendeddata.account.BalanceType.AVAILABLE;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;

@RequiredArgsConstructor
public class AmexGroupDataMapperV5 implements AmexGroupDataMapper {

    private final Clock clock;

    @Override
    public ProviderAccountDTO mapToAccount(@NotNull final Account accountResource,
                                           @Nullable final List<Balance> balances,
                                           @Nullable final Transactions transactions,
                                           @Nullable final Transactions pendingTransactions) {

        List<ProviderTransactionDTO> mappedTransactions = mapToTransactions(pendingTransactions, PENDING);
        mappedTransactions.addAll(mapToTransactions(transactions, BOOKED));
        ExtendedAccountDTO extendedAccount = mapToExtendedAccount(accountResource, balances);
        return ProviderAccountDTO.builder()
                .accountNumber(mapToAccountNumber(accountResource))
                .accountId(accountResource.getIdentifiers().getDisplayAccountNumber())
                .lastRefreshed(ZonedDateTime.now(clock))
                .currentBalance(getCurrentBalance(balances))
                .yoltAccountType(AccountType.CREDIT_CARD)
                .creditCardData(mapToCreditCardData(balances))
                .currency(getCurrency(balances))
                .closed(false)
                .accountMaskedIdentification(null)
                .name("Credit Card")
                .transactions(mappedTransactions)
                .extendedAccount(extendedAccount)
                .build();
    }

    private ExtendedAccountDTO mapToExtendedAccount(final Account account, final List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getIdentifiers().getDisplayAccountNumber())
                .accountReferences(mapToAccountReferences(account))
                .currency(getCurrency(balances))
                .name("Credit Card")
                .product(getProduct(account))
                .status(getAccountStatus(account))
                .balances(mapToBalances(balances))
                .build();
    }

    private List<BalanceDTO> mapToBalances(final List<Balance> balances) {
        if(balances == null) return null;

        List<BalanceDTO> mappedBalances = new ArrayList<>();
        for (Balance balance : balances) {
            mappedBalances.add(BalanceDTO.builder()
                                       .balanceAmount(new BalanceAmountDTO(CurrencyCode.valueOf(balance.getIsoAlphaCurrencyCode()), new BigDecimal(balance.getStatementBalanceAmount())))
                                       .balanceType(AVAILABLE)
                                       .build());
        }
        return mappedBalances;
    }

    private Status getAccountStatus(final Account account) {
        if (account.getStatus() == null || account.getStatus().getAccountStatus() == null) {
            return null;
        }
        List<String> statuses = account.getStatus().getAccountStatus();

        if (statuses.contains("Active") && !statuses.contains("Cancelled")) {
            return Status.ENABLED;
        }
        if (statuses.contains("Cancelled") && !statuses.contains("Active")) {
            return Status.DELETED;
        }
        return null;
    }

    private String getProduct(final Account account) {
        if (account.getProduct() != null && account.getProduct().getDigitalInfo() != null) {
            return account.getProduct().getDigitalInfo().getProductDesc();
        }
        return null;
    }

    private List<AccountReferenceDTO> mapToAccountReferences(final Account account) {
        List<AccountReferenceDTO> accountReferences = new ArrayList<>();
        accountReferences.add(new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, account.getIdentifiers().getDisplayAccountNumber()));
        if (account.getSupplementaryAccounts() != null) {
            for (AccountSupplementaryAccounts supplementaryAccounts : account.getSupplementaryAccounts()) {
                accountReferences.add(new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, supplementaryAccounts.getIdentifiers().getDisplayAccountNumber()));
            }
        }
        return accountReferences;
    }

    private ProviderCreditCardDTO mapToCreditCardData(List<Balance> balances) {
        return balances == null ? null : ProviderCreditCardDTO.builder()
                .dueDate(AmexDateTimeUtils.getZonedDateTime(balances.get(0).getPaymentDueDate()))
                .availableCreditAmount(new BigDecimal(balances.get(0).getDebitsBalanceAmount()))
                .build();
    }

    private List<ProviderTransactionDTO> mapToTransactions(Transactions transactions, TransactionStatus status) {
        if (transactions == null) {
            return Collections.emptyList();
        }
        List<ProviderTransactionDTO> transactionsDTO = new ArrayList<>();
        for (Transaction transaction : transactions.getTransactions()) {
            ProviderTransactionType type = ProviderTransactionType.valueOf(transaction.getType().toUpperCase());
            ProviderTransactionDTO providerTransactionDTO = ProviderTransactionDTO.builder()
                    .externalId(transaction.getIdentifier())
                    .amount((new BigDecimal(transaction.getAmount())).abs())
                    .description(mapToDescription(transaction))
                    .merchant(mapToMerchant(transaction))
                    .category(YoltCategory.GENERAL)
                    .status(status)
                    .type(type)
                    .extendedTransaction(mapToExtendedTransaction(transaction, status, type))
                    .dateTime(AmexDateTimeUtils.getZonedDateTime(transaction.getChargeDate()))
                    .build();
            transactionsDTO.add(providerTransactionDTO);
        }

        return transactionsDTO;
    }

    private String mapToDescription(final Transaction transaction) {
        if (transaction.getDescription() != null) {
            return transaction.getDescription();
        }
        return "N/A";
    }

    private ExtendedTransactionDTO mapToExtendedTransaction(final Transaction transaction, TransactionStatus status, ProviderTransactionType type) {
        TransactionForeignDetails foreignDetails = transaction.getForeignDetails();

        return ExtendedTransactionDTO.builder()
                .status(status)
                .entryReference(transaction.getReferenceNumber())
                .bookingDate(AmexDateTimeUtils.getZonedDateTime(transaction.getPostDate()))
                .valueDate(AmexDateTimeUtils.getZonedDateTime(transaction.getChargeDate()))
                .transactionAmount(mapToBalanceWithSign(transaction, type))
                .originalAmount(mapToOriginalAmount(foreignDetails))
                .exchangeRate(mapToExchangeRate(transaction.getForeignDetails()))
                .creditorName(getCreditorName(transaction, type))
                .debtorName(getDebtorName(transaction, type))
                .remittanceInformationUnstructured(transaction.getDescription())
                .transactionIdGenerated(false)
                .build();
    }

    private BalanceAmountDTO mapToOriginalAmount(final TransactionForeignDetails foreignDetails) {
        try {
            return foreignDetails != null && foreignDetails.getAmount() != null && foreignDetails.getIsoAlphaCurrencyCode() != null ?
                    new BalanceAmountDTO(CurrencyCode.valueOf(foreignDetails.getIsoAlphaCurrencyCode()), mapToBigDecimal(foreignDetails.getAmount())) : null;
        } catch (ParseException | NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal mapToBigDecimal(final String amount) throws ParseException {
        if (amount.matches("[0-9,]*\\.[0-9]{2}")) {
            DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
            decimalFormat.setParseBigDecimal(true);
            return (BigDecimal) decimalFormat.parse(amount);
        } else if (amount.matches("[0-9.]*,[0-9]{2}")) {
            DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.ITALIAN);
            decimalFormat.setParseBigDecimal(true);
            return (BigDecimal) decimalFormat.parse(amount);
        } else {
            return new BigDecimal(amount.replace(",", "."));
        }
    }

    private BalanceAmountDTO mapToBalanceWithSign(final Transaction transaction,
                                                  final ProviderTransactionType type) {
        BigDecimal amount = new BigDecimal(transaction.getAmount()).abs();
        if (ProviderTransactionType.DEBIT.equals(type)) {
            amount = amount.negate();
        }
        return new BalanceAmountDTO(CurrencyCode.valueOf(transaction.getIsoAlphaCurrencyCode()), amount);
    }

    private List<ExchangeRateDTO> mapToExchangeRate(final TransactionForeignDetails details) {
        if (details == null || details.getIsoAlphaCurrencyCode() == null) {
            return null; //NOSONAR in case there are no foreign details provided we do not want to initialize exchangeRate field at all.
        }
        return Arrays.asList(ExchangeRateDTO.builder()
                                     .currencyFrom(CurrencyCode.valueOf(details.getIsoAlphaCurrencyCode()))
                                     .rateFrom(details.getExchangeRate())
                                     .build());
    }

    private String getCreditorName(Transaction transaction, ProviderTransactionType type) {
        switch (type) {
            case CREDIT:
                return mapToMerchant(transaction);
            case DEBIT:
                return transaction.getFirstName() + transaction.getLastName();
            default:
                return null;
        }
    }

    private String getDebtorName(Transaction transaction, ProviderTransactionType type) {
        switch (type) {
            case CREDIT:
                return transaction.getFirstName() + transaction.getLastName();
            case DEBIT:
                return mapToMerchant(transaction);
            default:
                return null;
        }
    }

    private String mapToMerchant(Transaction transaction) {
        Optional<Merchant> merchant = Optional.ofNullable(transaction)
                .map(Transaction::getExtendedDetails)
                .map(ExtendedDetails::getMerchant);
        String merchantName = merchant.map(Merchant::getName)
                .orElse("N/A");

        String merchantAddress = merchant.map(Merchant::getAddress)
                .map(value -> StringUtils.collectionToCommaDelimitedString(value.getAddressLines()))
                .orElse("N/A");

        return String.format("%s %s", merchantName, merchantAddress);
    }

    private static ProviderAccountNumberDTO mapToAccountNumber(Account accountResource) {
        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(IBAN, accountResource.getIdentifiers().getDisplayAccountNumber());
        providerAccountNumberDTO.setHolderName(mapToHolderName(accountResource));
        providerAccountNumberDTO.setDescription(null);
        return providerAccountNumberDTO;
    }

    private static String mapToHolderName(Account accountResource) {
        Optional<Profile> profile = Optional.ofNullable(accountResource)
                .map(Account::getHolder)
                .map(HolderFragment::getProfile);

        return profile.map(p -> String.format("%s %s", p.getFirstName(), p.getLastName())).orElse("N/A");
    }

    private static CurrencyCode getCurrency(List<Balance> balances) {
        return balances == null ? null : CurrencyCode.valueOf(balances.get(0).getIsoAlphaCurrencyCode());
    }

    private static BigDecimal getCurrentBalance(List<Balance> balances) {
        return balances == null ? null : new BigDecimal(balances.get(0).getStatementBalanceAmount()).negate();
    }
}
