package com.yolt.providers.monorepogroup.qontogroup.common.mapper;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DefaultQontoGroupAccountMapperTest {

    private Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private String providerIdentifier = "PROVIDER_ID";
    private QontoGroupDateMapper dateMapper = new QontoGroupDateMapper(ZoneId.of("Europe/Paris"), clock);

    private DefaultQontoGroupAccountMapper accountMapper = new DefaultQontoGroupAccountMapper(providerIdentifier, dateMapper);

    @Test
    void shouldMappedAccount() {
        //given
        var accountFromBank = mock(Account.class);
        given(accountFromBank.getIban()).willReturn("IBAN1");
        given(accountFromBank.getSlug()).willReturn("slug-123");
        given(accountFromBank.getName()).willReturn("name");
        given(accountFromBank.getAuthorizedBalance()).willReturn(new BigDecimal("123.55"));
        given(accountFromBank.getBalance()).willReturn(new BigDecimal("144.88"));
        given(accountFromBank.getCurrency()).willReturn("EUR");
        given(accountFromBank.getBic()).willReturn("BIC");
        given(accountFromBank.getUpdatedAt()).willReturn(OffsetDateTime.of(2021, 12, 21, 10, 20, 00, 00, ZoneOffset.UTC));
        var expectedResult = ProviderAccountDTO.builder()
                .accountId("slug-123")
                .name("name")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .availableBalance(new BigDecimal("123.55"))
                .currentBalance(new BigDecimal("144.88"))
                .lastRefreshed(ZonedDateTime.of(2022, 01, 01, 00, 00, 00, 0, ZoneId.of("UTC")))
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "IBAN1"))
                .currency(CurrencyCode.EUR)
                .bic("BIC")
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("slug-123")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .name("name")
                        .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, "IBAN1")))
                        .bic("BIC")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("144.88"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .balanceType(BalanceType.INTERIM_BOOKED)
                                        .referenceDate(ZonedDateTime.of(2021, 12, 21, 11, 20, 00, 00, ZoneId.of("Europe/Paris")))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("123.55"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .referenceDate(ZonedDateTime.of(2021, 12, 21, 11, 20, 00, 00, ZoneId.of("Europe/Paris")))
                                        .build()))
                        .currency(CurrencyCode.EUR)
                        .build());

        //when
        var result = accountMapper.map(accountFromBank);

        //then
        assertThat(result.build()).isEqualTo(expectedResult.build());
    }

    @Test
    void shouldMapAccountWhenSomeFieldsAreMissing() {
        //given
        var accountFromBank = mock(Account.class);
        given(accountFromBank.getSlug()).willReturn("slug-123");
        given(accountFromBank.getAuthorizedBalance()).willReturn(new BigDecimal("123.55"));
        given(accountFromBank.getBalance()).willReturn(new BigDecimal("144.88"));
        given(accountFromBank.getBic()).willReturn("BIC");
        given(accountFromBank.getUpdatedAt()).willReturn(OffsetDateTime.of(2021, 12, 21, 10, 20, 00, 00, ZoneOffset.UTC));
        var expectedResult = ProviderAccountDTO.builder()
                .accountId("slug-123")
                .name("PROVIDER_ID Current Account")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .availableBalance(new BigDecimal("123.55"))
                .currentBalance(new BigDecimal("144.88"))
                .lastRefreshed(ZonedDateTime.of(2022, 01, 01, 00, 00, 00, 0, ZoneId.of("UTC")))
                .accountNumber(null)
                .currency(null)
                .bic("BIC")
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("slug-123")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .name("PROVIDER_ID Current Account")
                        .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, null)))
                        .bic("BIC")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("144.88"))
                                                .currency(null)
                                                .build())
                                        .balanceType(BalanceType.INTERIM_BOOKED)
                                        .referenceDate(ZonedDateTime.of(2021, 12, 21, 11, 20, 00, 00, ZoneId.of("Europe/Paris")))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("123.55"))
                                                .currency(null)
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .referenceDate(ZonedDateTime.of(2021, 12, 21, 11, 20, 00, 00, ZoneId.of("Europe/Paris")))
                                        .build()))
                        .currency(null)
                        .build());

        //when
        var result = accountMapper.map(accountFromBank);

        //then
        assertThat(result.build()).isEqualTo(expectedResult.build());
    }

}