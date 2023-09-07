package com.yolt.providers.volksbank.common.service.mapper;

import com.yolt.providers.volksbank.dto.v1_1.AccountDetails;
import com.yolt.providers.volksbank.dto.v1_1.Amount;
import com.yolt.providers.volksbank.dto.v1_1.BalanceItem;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class VolksbankDataMapperV1Test {

    VolksbankDataMapperService mapperService;
    BalanceItem balance;
    Amount amount;

    @BeforeEach
    public void setup() {
        VolksbankExtendedDataMapper extendedDataMapper = mock(VolksbankExtendedDataMapperV1.class);
        mapperService = new VolksbankDataMapperServiceV1(extendedDataMapper, new CurrencyCodeMapperV1(), Clock.systemUTC());
        balance = new BalanceItem();
        amount = new Amount();
        amount.setAmount("123");
        amount.setCurrency(Amount.CurrencyEnum.EUR);
        balance.setBalanceAmount(amount);
    }

    @Test
    public void shouldReturnNullIfCantMapCurrency() {
        // given
        AccountDetails accounts = new AccountDetails();
        accounts.setCurrency("Dummy currency that should not be mapped");

        // when
        ProviderAccountDTO dto = mapperService.mapToProviderAccountDTO(accounts, balance,
                null, "Volksbank");
        // then
        assertThat(dto.getCurrency()).isNull();
    }
}
