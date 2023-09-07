package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.unicredit.common.dto.UniCreditBalanceDTO;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
public class BalanceMapperV1 implements BalanceMapper {

    private final CurrencyCodeMapper currencyCodeMapper;
    private final ZoneId timeZoneId;

    @Override
    public BigDecimal getBalanceAmount(List<UniCreditBalanceDTO> balances, BalanceType balanceType) {
        return balances == null ? null : balances.stream().filter(it -> balanceType.equals(BalanceType.fromName(it.getBalanceType())))
                .map(it -> BigDecimal.valueOf(it.getAmount()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public BalanceDTO mapBalance(UniCreditBalanceDTO balance) {
        return BalanceDTO.builder()
                .balanceAmount(new BalanceAmountDTO(currencyCodeMapper.toCurrencyCode(balance.getCurrency()), BigDecimal.valueOf(balance.getAmount())))
                .balanceType(BalanceType.fromName(balance.getBalanceType()))
                .lastChangeDateTime(StringUtils.isEmpty(balance.getLastChangeDateTime()) ? null : parseDateTime(balance.getLastChangeDateTime()))
                .referenceDate(StringUtils.isEmpty(balance.getReferenceDate()) ? null : parseDate(balance.getReferenceDate()))
                .lastCommittedTransaction(balance.getLastCommittedTransaction())
                .build();
    }

    private ZonedDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime).atZone(timeZoneId);
    }

    private ZonedDateTime parseDate(String date) {
        return LocalDate.parse(date).atStartOfDay(timeZoneId);
    }
}
