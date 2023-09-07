package com.yolt.providers.commerzbankgroup.common.data.mapper;

import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.BalanceType;
import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.*;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;

@AllArgsConstructor
public class CommerzbankGroupDataMapperService implements CommerzbankGroupDataMapper {

    private Clock clock;
    public static final ZoneId BANK_TIMEZONE = ZoneId.of("Europe/Berlin");

    public ProviderAccountDTO toProviderAccountDTO(AccountDetails account, List<Transactions> transactionsList, ReadAccountBalanceResponse200 readAccountBalanceResponse200) {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .accountId(account.getResourceId())
                .lastRefreshed(ZonedDateTime.now(clock))
                .name(account.getName())
                .bic(account.getBic())
                .currency(CurrencyCode.valueOf(account.getCurrency()))
                .availableBalance(getAvailableBalance(readAccountBalanceResponse200.getBalances()))
                .currentBalance(getAvailableBalance(readAccountBalanceResponse200.getBalances()))
                .transactions(toProviderTransactionsDTOList(transactionsList))
                .accountNumber(toProviderAccountNumberDTO(account))
                .extendedAccount(toExtendedAccountDTO(account, readAccountBalanceResponse200))
                .build();
    }

    private ProviderAccountNumberDTO toProviderAccountNumberDTO(AccountDetails account) {
        if(StringUtils.isEmpty(account.getIban())) {
            return null;
        }
        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
        providerAccountNumberDTO.setHolderName(account.getOwnerName());
        return providerAccountNumberDTO;
    }

    private ExtendedAccountDTO toExtendedAccountDTO(AccountDetails account, ReadAccountBalanceResponse200 balance) {
        List<AccountReferenceDTO> accountReferenceDtos = toAccountReferencesDTO(account);
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(accountReferenceDtos)
                .currency(CurrencyCode.valueOf(account.getCurrency()))
                .name(account.getName())
                .product(account.getProduct())
                .cashAccountType(ExternalCashAccountType.fromCode(account.getCashAccountType()))
                .status(account.getStatus() == null ? null : Status.valueOf(account.getStatus().getValue()))
                .bic(account.getBic())
                .linkedAccounts(account.getLinkedAccounts())
                .usage(account.getUsage() == null ? null : UsageType.fromName(account.getUsage().getValue()))
                .details(account.getDetails())
                .balances(toBalanceDTOList(balance.getBalances()))
                .build();
    }

    private List<AccountReferenceDTO> toAccountReferencesDTO(AccountDetails account) {
        List<AccountReferenceDTO> references = new ArrayList<>();
        if (StringUtils.isNotEmpty(account.getIban())) {
            references.add(AccountReferenceDTO.builder().value(account.getIban()).type(AccountReferenceType.IBAN).build());
        }
        if (StringUtils.isNotEmpty(account.getBban())) {
            references.add(AccountReferenceDTO.builder().value(account.getBban()).type(AccountReferenceType.BBAN).build());
        }
        return references;
    }

    private List<BalanceDTO> toBalanceDTOList(List<Balance> balances) {
        if (CollectionUtils.isEmpty(balances))
            return null;
        return balances.stream()
                .filter(balance -> balance.getBalanceType().equals(BalanceType.CLOSINGBOOKED))
                .map(this::toBalanceDto).collect(Collectors.toList());
    }

    private BalanceDTO toBalanceDto(Balance balance) {
        return BalanceDTO.builder()
                .balanceType(nl.ing.lovebird.extendeddata.account.BalanceType.CLOSING_BOOKED)
                .balanceAmount(balance.getBalanceAmount() == null ? null :
                        toBalanceAmountDto(CurrencyCode.valueOf(balance.getBalanceAmount().getCurrency()),
                                new BigDecimal(balance.getBalanceAmount().getAmount())))
                .referenceDate(balance.getReferenceDate() == null ? null : balance.getReferenceDate().atStartOfDay(BANK_TIMEZONE))
                .lastChangeDateTime(balance.getLastChangeDateTime() == null ? null : balance.getLastChangeDateTime().toZonedDateTime())
                .build();
    }

    private BalanceAmountDTO toBalanceAmountDto(CurrencyCode currencyCode, BigDecimal availableBalance) {
        return BalanceAmountDTO.builder()
                .amount(availableBalance)
                .currency(currencyCode)
                .build();
    }

    @Override
    public List<ProviderTransactionDTO> toProviderTransactionsDTOList(List<Transactions> list) {
        return list.stream()
                .map(this::toProviderTransactionDTO)
                .collect(Collectors.toList());
    }

    private ProviderTransactionDTO toProviderTransactionDTO(Transactions transaction) {
        BigDecimal amount = new BigDecimal(transaction.getTransactionAmount().getAmount());
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .dateTime(transaction.getBookingDate().atStartOfDay(BANK_TIMEZONE))
                .amount(amount)
                .status(TransactionStatus.BOOKED)
                .type(amount.compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT)
                .description(transaction.getAdditionalInformation())
                .category(YoltCategory.GENERAL)
                .extendedTransaction(toExtendedTransactionDTO(transaction))
                .build();
    }

    private ExtendedTransactionDTO toExtendedTransactionDTO(Transactions transaction) {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.BOOKED)
                .entryReference(transaction.getTransactionId())
                .endToEndId(transaction.getEndToEndId())
                .mandateId(transaction.getMandateId())
                .checkId(transaction.getCheckId())
                .creditorId(transaction.getCreditorId())
                .bookingDate(transaction.getBookingDate().atStartOfDay(BANK_TIMEZONE))
                .valueDate(transaction.getValueDate().atStartOfDay(BANK_TIMEZONE))
                .transactionAmount(transaction.getTransactionAmount() == null ? null :
                        toBalanceAmountDto(CurrencyCode.valueOf(transaction.getTransactionAmount().getCurrency()),
                                new BigDecimal(transaction.getTransactionAmount().getAmount())))
                .creditorAccount(transaction.getCreditorAccount() == null ? null : toAccountReferenceDTO(transaction.getCreditorAccount()))
                .creditorName(transaction.getCreditorName())
                .ultimateCreditor(transaction.getUltimateCreditor())
                .debtorAccount(transaction.getDebtorAccount() == null ? null : toAccountReferenceDTO(transaction.getDebtorAccount()))
                .debtorName(transaction.getDebtorName())
                .ultimateDebtor(transaction.getUltimateDebtor())
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
                .remittanceInformationStructured(transaction.getRemittanceInformationStructured())
                .purposeCode(transaction.getPurposeCode() == null ? null : transaction.getPurposeCode().getValue())
                .bankTransactionCode(transaction.getBankTransactionCode())
                .transactionIdGenerated(false)
                .build();
    }

    private AccountReferenceDTO toAccountReferenceDTO(AccountReference accountReference) {
        if (StringUtils.isNotEmpty(accountReference.getIban())) {
            return AccountReferenceDTO.builder()
                    .type(AccountReferenceType.IBAN)
                    .value(accountReference.getIban())
                    .build();
        } else if (StringUtils.isNotEmpty(accountReference.getBban())) {
            return AccountReferenceDTO.builder()
                    .type(AccountReferenceType.BBAN)
                    .value(accountReference.getBban())
                    .build();
        }
        return null;
    }

    private BigDecimal getAvailableBalance(List<Balance> balances) {
        if (CollectionUtils.isEmpty(balances))
            return null;
        return balances.stream()
                .filter(balance -> balance.getBalanceType().equals(BalanceType.CLOSINGBOOKED))
                .findFirst()
                .map(balance -> new BigDecimal(balance.getBalanceAmount().getAmount()))
                .orElse(null);
    }

}
