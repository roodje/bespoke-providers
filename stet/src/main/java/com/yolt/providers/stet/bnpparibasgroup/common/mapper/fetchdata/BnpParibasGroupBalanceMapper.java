package com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata;

import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yolt.providers.stet.generic.dto.balance.StetBalanceType.OTHR;

public class BnpParibasGroupBalanceMapper {

    public BigDecimal extractBalanceAmount(List<StetBalanceDTO> balanceResources,
                                           CurrencyCode supportedCurrency,
                                           List<StetBalanceType> preferredBalances) {
        Map<StetBalanceType, BigDecimal> balanceAmounts = balanceResources.stream()
                .collect(Collectors.toMap(StetBalanceDTO::getType, StetBalanceDTO::getAmount));

        if (!ObjectUtils.isEmpty(supportedCurrency)) {
            List<StetBalanceDTO> filteredBalanceResources = balanceResources.stream()
                    .filter(balanceResource -> supportedCurrency.equals(balanceResource.getCurrency()))
                    .collect(Collectors.toList());

            if (balanceResources.isEmpty()) {
                return null;
            }
            if (filteredBalanceResources.isEmpty()) {
                return null;
            }
            if (filteredBalanceResources.size() == 1) {
                return filteredBalanceResources.get(0).getAmount();
            }

            for (StetBalanceType preferedBalanceType : preferredBalances) {
                if (balanceAmounts.containsKey(preferedBalanceType)) {
                    return balanceAmounts.get(preferedBalanceType);
                }
            }
        }
        return null;
    }

    public List<BalanceDTO> mapToBalanceDTOs(List<StetBalanceDTO> balanceResources, CurrencyCode supportedCurrency) {
        if (!ObjectUtils.isEmpty(supportedCurrency)) {
            return balanceResources.stream()
                    .filter(balanceResource -> supportedCurrency.equals(balanceResource.getCurrency()))
                    .filter(balance -> !OTHR.equals(balance.getType()))
                    .map(balance -> mapToBalanceDTO(balance, convertToBalanceType(balance.getType())))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private BalanceDTO mapToBalanceDTO(StetBalanceDTO balance, BalanceType balanceType) {
        return BalanceDTO.builder()
                .balanceType(balanceType)
                .balanceAmount(mapToBalanceAmountDTO(balance))
                .referenceDate(convertToZonedDateTime(balance.getReferenceDate()))
                .build();
    }

    private ZonedDateTime convertToZonedDateTime(OffsetDateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(date -> dateTime.atZoneSameInstant(ZoneId.of("Z")))
                .orElse(null);
    }

    private BalanceAmountDTO mapToBalanceAmountDTO(StetBalanceDTO balance) {
        return BalanceAmountDTO.builder()
                .amount(balance.getAmount())
                .currency(balance.getCurrency())
                .build();
    }

    private BalanceType convertToBalanceType(StetBalanceType balanceType) {
        switch (balanceType) {
            case CLBD:
                return BalanceType.CLOSING_BOOKED;
            case XPCD:
                return BalanceType.INTERIM_AVAILABLE;
            default:
                return null;
        }
    }
}
