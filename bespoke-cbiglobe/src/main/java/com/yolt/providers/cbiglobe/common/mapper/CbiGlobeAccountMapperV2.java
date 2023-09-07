package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadAccountBalanceResponseTypeBalances;
import com.yolt.providers.cbiglobe.dto.ReadAccountListResponseTypeAccounts;
import com.yolt.providers.cbiglobe.dto.TransactionsReadaccounttransactionlistType1;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nl.ing.lovebird.extendeddata.account.BalanceType.*;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;

@AllArgsConstructor
public class CbiGlobeAccountMapperV2 implements CbiGlobeAccountMapper {

    private final CbiGlobeBalanceMapper balanceMapper;
    private final CbiGlobeTransactionMapper transactionMapper;
    private final CbiGlobeExtendedAccountMapper extendedAcountMapper;
    private final CurrencyCodeMapper currencyMapper;
    private final Clock clock;

    public ProviderAccountDTO mapToProviderAccountDTO(ReadAccountListResponseTypeAccounts account) {
        return ProviderAccountDTO.builder()
                .lastRefreshed(Instant.now(clock).atZone(clock.getZone()))
                .yoltAccountType(CURRENT_ACCOUNT)
                .accountId(account.getResourceId())
                .accountMaskedIdentification(account.getMaskedPan())
                .accountNumber(mapProviderAccountNumberDtoFrom(account))
                .bic(account.getBic())
                .name(StringUtils.defaultIfEmpty(account.getName(), account.getResourceId()))
                .currency(currencyMapper.toCurrencyCode(account.getCurrency()))
                .closed(false)
                .extendedAccount(extendedAcountMapper.mapExtendedAccountDtoFromCurrentAccount(account))
                .build();
    }

    private ProviderAccountNumberDTO mapProviderAccountNumberDtoFrom(ReadAccountListResponseTypeAccounts accountDetails) {
        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(Scheme.IBAN, accountDetails.getIban());
        providerAccountNumberDTO.setHolderName(accountDetails.getName());
        providerAccountNumberDTO.setDescription(accountDetails.getDetails());
        return providerAccountNumberDTO;
    }


    @Override
    public ProviderAccountDTO updateProviderAccountDTO(ProviderAccountDTO account,
                                                       List<ReadAccountBalanceResponseTypeBalances> balances,
                                                       List<TransactionsReadaccounttransactionlistType1> transactions) {
        List<BalanceDTO> balanceDTOs = balances.stream()
                .map(balanceMapper::mapToBalanceDTO)
                .collect(Collectors.toList());

        List<ProviderTransactionDTO> transactionDTOs = transactions.stream()
                .map(transactionMapper::mapToProviderTransactionDTOs)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return account.toBuilder()
                .currentBalance(balanceMapper.extractBalanceAmount(balanceDTOs, account.getCurrency(), getCurrentPreferredBalanceTypes()))
                .availableBalance(balanceMapper.extractBalanceAmount(balanceDTOs, account.getCurrency(), getAvailablePreferredBalanceTypes()))
                .transactions(transactionDTOs)
                .extendedAccount(account.getExtendedAccount().toBuilder()
                        .balances(balanceDTOs)
                        .build())
                .build();
    }

    /**
     * The priority is maintained according to the elements added.
     * The first element has the highest priority
     */
    protected List<BalanceType> getCurrentPreferredBalanceTypes() {
        return Arrays.asList(INTERIM_BOOKED, CLOSING_BOOKED, OPENING_BOOKED, EXPECTED);
    }

    /**
     * The priority is maintained according to the elements added.
     * The first element has the highest priority
     */
    protected List<BalanceType> getAvailablePreferredBalanceTypes() {
        return Arrays.asList(EXPECTED, INTERIM_AVAILABLE, AVAILABLE, FORWARD_AVAILABLE, AUTHORISED);
    }
}
