package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Balance;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.RaiffeisenBalanceType;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupAccountMapperTest {

    private DefaultRaiffeisenAtGroupAccountMapper accountMapper;

    private ZoneId zoneId = ZoneId.of("Europe/Vienna");
    private Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        accountMapper = new DefaultRaiffeisenAtGroupAccountMapper("RAIFFEISEN_AT",
                new DefaultRaiffeisenAtGroupDateMapper(zoneId,
                        clock));
    }

    @Test
    void shouldMapAccount() {
        //given
        var forwardAvailableBalance = mock(Balance.class);
        given(forwardAvailableBalance.getAmount()).willReturn(new BigDecimal("134.55"));
        given(forwardAvailableBalance.getCurrency()).willReturn("EUR");
        given(forwardAvailableBalance.getBalanceType()).willReturn(RaiffeisenBalanceType.FORWARD_AVAILABLE);
        given(forwardAvailableBalance.getReferenceDate()).willReturn(LocalDate.of(2022, 07, 06));
        var interimAvailableBalance = mock(Balance.class);
        given(interimAvailableBalance.getAmount()).willReturn(new BigDecimal("155.44"));
        given(interimAvailableBalance.getCurrency()).willReturn("EUR");
        given(interimAvailableBalance.getBalanceType()).willReturn(RaiffeisenBalanceType.INTERIM_AVAILABLE);
        given(interimAvailableBalance.getReferenceDate()).willReturn(LocalDate.of(2022, 07, 05));
        var accountFromBank = mock(Account.class);
        given(accountFromBank.getResourceId()).willReturn("AT099900000000001511");
        given(accountFromBank.getIban()).willReturn("AT099900000000001511");
        given(accountFromBank.getCurrency()).willReturn("EUR");
        given(accountFromBank.getName()).willReturn("Superman");
        given(accountFromBank.getProduct()).willReturn("Giro");
        given(accountFromBank.getBic()).willReturn("TESTAT99");
        given(accountFromBank.getBalances()).willReturn(List.of(forwardAvailableBalance, interimAvailableBalance));

        var expectedMappedAccount = ProviderAccountDTO.builder()
                .accountId("AT099900000000001511")
                .name("Superman")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .availableBalance(new BigDecimal("134.55"))
                .currentBalance(new BigDecimal("155.44"))
                .lastRefreshed(ZonedDateTime.now(clock))
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "AT099900000000001511"))
                .currency(CurrencyCode.EUR)
                .bic("TESTAT99")
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("AT099900000000001511")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .name("Superman")
                        .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, "AT099900000000001511")))
                        .bic("TESTAT99")
                        .balances(List.of(BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("134.55"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .balanceType(BalanceType.FORWARD_AVAILABLE)
                                        .referenceDate(LocalDate.of(2022, 07, 06).atStartOfDay(zoneId))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("155.44"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .referenceDate(LocalDate.of(2022, 07, 05).atStartOfDay(zoneId))
                                        .build()))
                        .product("Giro")
                        .currency(CurrencyCode.EUR).build())
                .build();

        //when
        var result = accountMapper.map(accountFromBank);

        //then
        assertThat(result.build()).isEqualTo(expectedMappedAccount);
    }

    @Test
    void shouldMapAccountWithFallbackAccountName() {
        //given
        var accountFromBank = mock(Account.class);
        given(accountFromBank.getIban()).willReturn("");

        //when
        var result = accountMapper.map(accountFromBank);

        //then
        assertThat(result.build().getName()).isEqualTo("RAIFFEISEN_AT Current Account");
    }

    @Test
    void shouldMapAccountWithoutCurrency() {
        //given
        var accountFromBank = mock(Account.class);
        given(accountFromBank.getCurrency()).willReturn("");

        //when
        var result = accountMapper.map(accountFromBank);

        //then
        assertThat(result.build().getCurrency()).isNull();

    }
}