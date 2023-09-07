package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadCardAccountBalancesResponseTypeBalances;
import com.yolt.providers.cbiglobe.dto.ReadCardAccountListResponseTypeCardAccounts;
import com.yolt.providers.cbiglobe.dto.ReadCardAccountTransactionListResponseTypeTransactions;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderCreditCardDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nl.ing.lovebird.extendeddata.account.BalanceType.*;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;

@RequiredArgsConstructor
public class CbiGlobeCardAccountMapperV1 implements CbiGlobeCardAccountMapper {

    private final CbiGlobeCardBalanceMapper cardBalanceMapper;
    private final CbiGlobeCardTransactionMapper cardTransactionMapper;
    private final CbiGlobeExtendedCardAccountMapper extendedCardAccountMapper;
    private final CurrencyCodeMapper currencyMapper;
    private final Clock clock;

    @Override
    public ProviderAccountDTO mapToProviderAccountDTO(ReadCardAccountListResponseTypeCardAccounts account) {
        new ProviderCreditCardDTO();
        return ProviderAccountDTO.builder()
                .lastRefreshed(Instant.now(clock).atZone(clock.getZone()))
                .yoltAccountType(CREDIT_CARD)
                .accountId(account.getResourceId())
                .accountMaskedIdentification(account.getMaskedPan())
                .creditCardData(ProviderCreditCardDTO.builder()
                        .availableCreditAmount(new BigDecimal(account.getCreditLimit().getAmount()))
                        .build())
//                .accountNumber(mapProviderAccountNumberDtoFrom(account))
                .name(StringUtils.defaultIfEmpty(account.getName(), account.getResourceId()))
                .currency(currencyMapper.toCurrencyCode(account.getCurrency()))
                .closed(false)
                .extendedAccount(extendedCardAccountMapper.mapExtendedAccountDtoFromCurrentAccount(account))
                .build();
    }

    @Override
    public ProviderAccountDTO updateProviderAccountDTO(ProviderAccountDTO account,
                                                       List<ReadCardAccountBalancesResponseTypeBalances> balances,
                                                       List<ReadCardAccountTransactionListResponseTypeTransactions> transactions) {
        List<BalanceDTO> balanceDTOs = balances.stream()
                .map(cardBalanceMapper::mapToBalanceDTO)
                .collect(Collectors.toList());

        List<ProviderTransactionDTO> transactionDTOs = transactions.stream()
                .map(cardTransactionMapper::mapToProviderTransactionDTOs)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return account.toBuilder()
                .currentBalance(cardBalanceMapper.extractBalanceAmount(balanceDTOs, account.getCurrency(), getCurrentPreferredBalanceTypes()))
                .availableBalance(cardBalanceMapper.extractBalanceAmount(balanceDTOs, account.getCurrency(), getAvailablePreferredBalanceTypes()))
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
