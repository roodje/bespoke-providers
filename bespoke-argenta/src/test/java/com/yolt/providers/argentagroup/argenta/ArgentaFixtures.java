package com.yolt.providers.argentagroup.argenta;

import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ArgentaFixtures {

    public static ProviderAccountDTO account1Fixture(Clock clock) {
        return newValidCurrentAccountDTOBuilder(
                "account1",
                new BigDecimal("9700.00"),
                new BigDecimal("9600.00"),
                new BigDecimal("0.00"),
                "BE98999000661993",
                "999000661993",
                "test FKI 2505",
                clock
        ).build();
    }

    public static ProviderAccountDTO account2Fixture(Clock clock) {
        return newValidCurrentAccountDTOBuilder(
                "account2",
                new BigDecimal("700.00"),
                new BigDecimal("800.00"),
                new BigDecimal("1.00"),
                "BE60999000126170",
                "999000126170",
                "Current account 2",
                clock
        ).build();
    }

    public static ProviderTransactionDTO account1BookedTransactionIndex0Fixture() {
        return ProviderTransactionDTO.builder()
                .externalId("B7H31CWDOS4K1F13")
                .dateTime(ZonedDateTime.ofInstant(Instant.parse("2020-12-31T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.of("Europe/Brussels")))
                .amount(new BigDecimal("8637.31"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.CREDIT)
                .description("PSQDNJPUDAUHVLAVXZIH")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .entryReference("B7H31CWDOS4K1F13")
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(ZonedDateTime.ofInstant(Instant.parse("2020-12-31T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.of("Europe/Brussels")))
                        .transactionAmount(
                                new BalanceAmountDTO(
                                        CurrencyCode.EUR,
                                        new BigDecimal("8637.31")
                                )
                        )
                        .debtorAccount(new AccountReferenceDTO(AccountReferenceType.IBAN, "BE50999090049618"))
                        .debtorName("KMUJGMCVHOEWCCCPDGLO")
                        .remittanceInformationUnstructured("PSQDNJPUDAUHVLAVXZIH")
                        .remittanceInformationStructured("")
                        .proprietaryBankTransactionCode("P7MT9W")
                        .build()
                )
                .build();
    }

    public static ProviderTransactionDTO account1BookedTransactionIndex1Fixture() {
        return ProviderTransactionDTO.builder()
                .externalId("B7H31CWDOQ7K1CLR")
                .dateTime(ZonedDateTime.ofInstant(Instant.parse("2020-12-31T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.of("Europe/Brussels")))
                .amount(new BigDecimal("6843.46"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("VCUTZERMRSQPNGSMWCAG")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .entryReference("B7H31CWDOQ7K1CLR")
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(ZonedDateTime.ofInstant(Instant.parse("2020-12-31T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.of("Europe/Brussels")))
                        .transactionAmount(
                                new BalanceAmountDTO(
                                        CurrencyCode.EUR,
                                        new BigDecimal("6843.46")
                                )
                        )
                        .creditorAccount(new AccountReferenceDTO(AccountReferenceType.IBAN, "BE50999090049618"))
                        .creditorName("ENZCASJWXSNWOOHRSGIT")
                        .entryReference("B7H31CWDOQ7K1CLR")
                        .remittanceInformationStructured("VCUTZERMRSQPNGSMWCAG")
                        .remittanceInformationUnstructured("")
                        .proprietaryBankTransactionCode("JF")
                        .build()
                )
                .build();
    }

    public static ProviderTransactionDTO account1PendingTransactionIndex5Fixture() {
        return ProviderTransactionDTO.builder()
                .externalId("B7H31CWDOP4K1B09")
                .dateTime(ZonedDateTime.ofInstant(Instant.parse("2020-12-30T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.of("Europe/Brussels")))
                .amount(new BigDecimal("4343.21"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.DEBIT)
                .description("JITKAZGKSRRDSNNHAOEN")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .entryReference("B7H31CWDOP4K1B09")
                        .status(TransactionStatus.PENDING)
                        .transactionAmount(
                                new BalanceAmountDTO(
                                        CurrencyCode.EUR,
                                        new BigDecimal("4343.21")
                                )
                        )
                        .creditorName("RLPBADOUNJXTXMIEHKIB")
                        .creditorAccount(new AccountReferenceDTO(AccountReferenceType.IBAN, "BE50999090049618"))
                        .proprietaryBankTransactionCode("8R5699")
                        .remittanceInformationUnstructured("JITKAZGKSRRDSNNHAOEN")
                        .remittanceInformationStructured("")
                        .build()
                )
                .build();
    }

    private static ProviderAccountDTO.ProviderAccountDTOBuilder newValidCurrentAccountDTOBuilder(final String accountId,
                                                                                                 final BigDecimal availableBalance,
                                                                                                 final BigDecimal currentBalance,
                                                                                                 final BigDecimal previouslyClosedBooked,
                                                                                                 final String iban,
                                                                                                 final String bban,
                                                                                                 final String accountName,
                                                                                                 final Clock clock) {
        return new ProviderAccountDTO(
                AccountType.CURRENT_ACCOUNT,
                ZonedDateTime.now(clock),
                availableBalance,
                currentBalance,
                accountId,
                null,
                new ProviderAccountNumberDTO(
                        ProviderAccountNumberDTO.Scheme.IBAN,
                        iban
                ),
                null,
                accountName,
                CurrencyCode.EUR,
                Boolean.FALSE,
                null,
                Collections.emptyList(),
                null,
                null,
                newValidExtendedAccountDTOBuilder()
                        .resourceId(accountId)
                        .name(accountName)
                        .currency(CurrencyCode.EUR)
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .balances(
                                List.of(
                                        new BalanceDTO(
                                                new BalanceAmountDTO(CurrencyCode.EUR, currentBalance),
                                                BalanceType.INTERIM_BOOKED,
                                                null,
                                                LocalDate.parse("2019-02-04").atStartOfDay(ZoneOffset.UTC),
                                                null
                                        ),
                                        new BalanceDTO(
                                                new BalanceAmountDTO(CurrencyCode.EUR, availableBalance),
                                                BalanceType.INTERIM_AVAILABLE,
                                                null,
                                                LocalDate.parse("2019-02-04").atStartOfDay(ZoneOffset.UTC),
                                                null),
                                        new BalanceDTO(
                                                new BalanceAmountDTO(CurrencyCode.EUR, previouslyClosedBooked),
                                                BalanceType.PREVIOUSLY_CLOSED_BOOKED,
                                                null,
                                                LocalDate.parse("2019-02-04").atStartOfDay(ZoneOffset.UTC),
                                                null
                                        )
                                )
                        )
                        .accountReferences(
                                List.of(
                                        new AccountReferenceDTO(AccountReferenceType.IBAN, iban),
                                        new AccountReferenceDTO(AccountReferenceType.BBAN, bban)
                                )
                        )
                        .build()
                ,
                null,
                null
        ).toBuilder();
    }

    private static ExtendedAccountDTO.ExtendedAccountDTOBuilder newValidExtendedAccountDTOBuilder() {
        return new ExtendedAccountDTO(
                null,
                Arrays.asList(
                        new AccountReferenceDTO(AccountReferenceType.IBAN, UUID.randomUUID().toString())
                ),
                CurrencyCode.EUR,
                null,
                "Current account",
                null,
                null,
                null,
                null,
                null,
                null,
                Arrays.asList(
                        new BalanceDTO(
                                new BalanceAmountDTO(CurrencyCode.GBP, BigDecimal.ZERO),
                                BalanceType.AVAILABLE,
                                ZonedDateTime.now(),
                                ZonedDateTime.now(),
                                null),
                        new BalanceDTO(
                                new BalanceAmountDTO(CurrencyCode.GBP, BigDecimal.ONE),
                                BalanceType.CLOSING_BOOKED,
                                ZonedDateTime.now(),
                                ZonedDateTime.now(),
                                null
                        )
                )
        ).toBuilder();
    }
}
