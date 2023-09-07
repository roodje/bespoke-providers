package com.yolt.providers.volksbank.common.service.mapper;

import com.yolt.providers.volksbank.dto.v1_1.AccountDetails;
import com.yolt.providers.volksbank.dto.v1_1.Amount;
import com.yolt.providers.volksbank.dto.v1_1.BalanceItem;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class VolksbankExtendedDataMapperV1Test {

    VolksbankExtendedDataMapperV1 mapperService;
    BalanceItem balance;
    Amount amount;

    @BeforeEach
    public void setup() {
        mapperService = new VolksbankExtendedDataMapperV1(new CurrencyCodeMapperV1());
        balance = new BalanceItem();
        amount = new Amount();
        amount.setAmount("123");
        amount.setCurrency(Amount.CurrencyEnum.EUR);
        balance.setBalanceAmount(amount);
        balance.setBalanceType(BalanceItem.BalanceTypeEnum.INTERIMAVAILABLE);
        balance.setLastChangeDateTime(OffsetDateTime.now());
    }

    @Test
    public void shouldReturnNullIfCantMapCurrency() {
        // given
        AccountDetails accounts = new AccountDetails();
        accounts.setCurrency("Dummy currency that should not be mapped");

        // when
        ExtendedAccountDTO dto = mapperService.createExtendedAccountDTO(accounts, balance);

        // then
        assertThat(dto.getCurrency()).isNull();
    }
}
