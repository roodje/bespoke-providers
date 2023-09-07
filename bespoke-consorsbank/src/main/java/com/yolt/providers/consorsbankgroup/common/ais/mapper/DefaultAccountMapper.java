package com.yolt.providers.consorsbankgroup.common.ais.mapper;

import com.yolt.providers.consorsbankgroup.dto.AccountDetails;
import com.yolt.providers.consorsbankgroup.dto.AccountStatus;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static com.yolt.providers.consorsbankgroup.consorsbank.ConsorsbankBeanConfig.ZONE_ID;

@RequiredArgsConstructor
public class DefaultAccountMapper {

    private final DefaultExtendedModelAccountMapper extendedModelAccountMapper;

    private final String accountName;
    private final Clock clock;

    public ProviderAccountDTO mapAccount(final AccountDetails account,
                                         final List<ProviderTransactionDTO> transactionDTOS,
                                         final Map<BalanceType, BalanceDTO> balanceMap) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .lastRefreshed(Instant.now(clock).atZone(ZoneId.of("UTC")))
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban()))
                .transactions(transactionDTOS)
                .currentBalance(getCurrentBalance(balanceMap))
                .availableBalance(getAvailableBalance(balanceMap))
                .name(accountName)
                .closed(account.getStatus() != null && !AccountStatus.ENABLED.equals(account.getStatus()))
                .currency(CurrencyCode.valueOf(account.getCurrency()))
                .bic(account.getBic())
                .yoltAccountType(mapAccountType(account))
                .extendedAccount(extendedModelAccountMapper.mapAccount(account, balanceMap, accountName))
                .build();
    }

    private BigDecimal getAvailableBalance(final Map<BalanceType, BalanceDTO> balanceMap) {
        return balanceMap.get(BalanceType.INTERIM_AVAILABLE).getBalanceAmount().getAmount();
    }

    private BigDecimal getCurrentBalance(final Map<BalanceType, BalanceDTO> balanceMap) {
        return balanceMap.get(BalanceType.CLOSING_BOOKED).getBalanceAmount().getAmount();
    }

    public AccountType mapAccountType(final AccountDetails accountDetails) {
        if ("CACC".equalsIgnoreCase(accountDetails.getCashAccountType())) {
            return AccountType.CURRENT_ACCOUNT;
        } else if ("SVGS".equalsIgnoreCase(accountDetails.getCashAccountType())) {
            return AccountType.SAVINGS_ACCOUNT;
        } else if ("TRAS".equalsIgnoreCase(accountDetails.getCashAccountType())) {
            return AccountType.INVESTMENT;
        } else {
            return null;
        }
    }
}
