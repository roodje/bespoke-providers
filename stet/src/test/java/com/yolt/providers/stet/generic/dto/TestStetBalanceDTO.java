package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import lombok.Builder;
import lombok.Getter;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class TestStetBalanceDTO implements StetBalanceDTO {

    private String name;
    private BigDecimal amount;
    private CurrencyCode currency;
    private StetBalanceType type;
    private OffsetDateTime lastChangeDateTime;
    private OffsetDateTime referenceDate;
    private String lastCommittedTransaction;
}
