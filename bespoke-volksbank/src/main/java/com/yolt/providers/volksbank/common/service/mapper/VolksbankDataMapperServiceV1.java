package com.yolt.providers.volksbank.common.service.mapper;

import com.yolt.providers.volksbank.dto.v1_1.AccountDetails;
import com.yolt.providers.volksbank.dto.v1_1.BalanceItem;
import com.yolt.providers.volksbank.dto.v1_1.RemittanceInformationStructured;
import com.yolt.providers.volksbank.dto.v1_1.TransactionItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;

@Slf4j
@AllArgsConstructor
public class VolksbankDataMapperServiceV1 implements VolksbankDataMapperService {

    private static final ZoneId AMSTERDAM_ZONE_ID = ZoneId.of("Europe/Amsterdam");
    private final VolksbankExtendedDataMapper extendedDataMapper;
    private final CurrencyCodeMapper currencyCodeMapper;
    private final Clock clock;

    @Override
    public ProviderAccountDTO mapToProviderAccountDTO(final AccountDetails account,
                                                      final BalanceItem accountBalance,
                                                      final List<ProviderTransactionDTO> transactionsConverted,
                                                      final String providerName) {

        BigDecimal balance = new BigDecimal(accountBalance.getBalanceAmount().getAmount());
        String name = account.getName();
        if (StringUtils.isEmpty(name)) {
            name = account.getProduct();
        }
        if (StringUtils.isEmpty(name)) {
            name = providerName;
        }

        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(balance)
                .currentBalance(balance)
                .accountId(account.getResourceId())
                .accountNumber(mapToProviderAccountNumber(account))
                .name(name)
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .bic(account.getCustomerBic())
                .transactions(transactionsConverted)
                .extendedAccount(extendedDataMapper.createExtendedAccountDTO(account, accountBalance))
                .build();
    }

    @Override
    public ProviderTransactionDTO mapToProviderTransactionDTO(final TransactionItem transaction) {
        BigDecimal amount = new BigDecimal(transaction.getTransactionAmount().getAmount());
        ProviderTransactionType type = amount.compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT;

        return ProviderTransactionDTO.builder()
                .externalId(transaction.getEntryReference())
                .dateTime(transaction.getBookingDate().atStartOfDay(AMSTERDAM_ZONE_ID))
                .type(type)
                .category(YoltCategory.GENERAL)
                .amount(amount.abs())
                .description(mapDescription(transaction.getRemittanceInformationUnstructured(), transaction.getRemittanceInformationStructured()))
                .status(TransactionStatus.BOOKED)
                .extendedTransaction(extendedDataMapper.createExtendedTransactionDTO(transaction, amount))
                .build();
    }

    private String mapDescription(String remittanceInformationUnstructured, RemittanceInformationStructured remittanceInformationStructured) {
        if (StringUtils.isNotEmpty(remittanceInformationUnstructured)) {
            return remittanceInformationUnstructured;
        }
        if (ObjectUtils.isNotEmpty(remittanceInformationStructured)) {
            return String.format("%s %s", remittanceInformationStructured.getReferenceIssuer(), remittanceInformationStructured.getReference());
        }
        return "N/A";
    }

    private ProviderAccountNumberDTO mapToProviderAccountNumber(final AccountDetails account) {
        ProviderAccountNumberDTO providerAccountNumber = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
        providerAccountNumber.setHolderName(account.getOwnerName());
        return providerAccountNumber;
    }
}
