package com.yolt.providers.stet.generic.mapper.account;

import com.yolt.providers.stet.generic.dto.TestStetAccountDTO;
import com.yolt.providers.stet.generic.dto.TestStetBalanceDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountType;
import com.yolt.providers.stet.generic.dto.account.StetAccountUsage;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.yolt.providers.stet.generic.dto.account.StetAccountType.CARD;
import static com.yolt.providers.stet.generic.dto.balance.StetBalanceType.*;
import static nl.ing.lovebird.extendeddata.common.CurrencyCode.EUR;
import static nl.ing.lovebird.extendeddata.common.CurrencyCode.GBP;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultAccountMapperTest {

    private static final String ACCOUNT_IBAN = "FR4230003000306327748225F14";

    private DefaultAccountMapper accountMapper;

    @BeforeEach
    void initialize() {
        accountMapper = new DefaultAccountMapper(new DateTimeSupplier(Clock.systemUTC()));
    }

    @Test
    void shouldMapToProviderAccountDTO() {
        // given
        StetAccountDTO accountDTO = createStetAccountDTO(ACCOUNT_IBAN, EUR);
        List<ProviderTransactionDTO> transactionDTOs = Collections.emptyList();
        List<StetBalanceDTO> balanceDTOs = List.of(
                createStetBalanceDTO(XPCD, "100.55", EUR),
                createStetBalanceDTO(CLBD, "267.82", EUR));

        // when
        ProviderAccountDTO providerAccountDTO = accountMapper.mapToProviderAccountDTO(accountDTO, balanceDTOs, transactionDTOs);

        // then
        assertThat(providerAccountDTO).satisfies(validateAccountDTO(accountDTO));
    }

    @Test
    void shouldMapToExtendedAccountDTO() {
        // given
        StetAccountDTO accountDTO = createStetAccountDTO(ACCOUNT_IBAN, EUR);
        List<StetBalanceDTO> balanceDTOs = List.of(
                createStetBalanceDTO(XPCD, "100.55", EUR),
                createStetBalanceDTO(CLBD, "267.82", EUR));

        // when
        ExtendedAccountDTO extendedAccountDTO = accountMapper.mapToExtendedAccountDTO(accountDTO, balanceDTOs);

        // then
        assertThat(extendedAccountDTO).satisfies(validateExtendedAccountDTO(accountDTO));
    }

    @Test
    void shouldMapToCurrencyCodeBasedOnAccount() {
        // given
        StetAccountDTO account = createStetAccountDTO(ACCOUNT_IBAN, EUR);
        List<StetBalanceDTO> balances = Collections.singletonList(
                createStetBalanceDTO(XPCD, GBP));

        // when
        CurrencyCode currencyCode = accountMapper.mapToCurrencyCode(account, balances);

        // then
        assertThat(currencyCode).isEqualTo(EUR);
    }

    @Test
    void shouldMapToCurrencyCodeBasedOnBalances() {
        // given
        StetAccountDTO account = createStetAccountDTO(ACCOUNT_IBAN, null);
        List<StetBalanceDTO> balances = List.of(
                createStetBalanceDTO(XPCD, GBP),
                createStetBalanceDTO(CLBD, GBP));

        // when
        CurrencyCode currencyCode = accountMapper.mapToCurrencyCode(account, balances);

        // then
        assertThat(currencyCode).isEqualTo(GBP);
    }

    @Test
    void shouldMapToCurrencyCodeAsMultiCurrency() {
        // given
        StetAccountDTO account = createStetAccountDTO(ACCOUNT_IBAN, null);
        List<StetBalanceDTO> balances = List.of(
                createStetBalanceDTO(XPCD, EUR),
                createStetBalanceDTO(CLBD, GBP));

        // when
        CurrencyCode currencyCode = accountMapper.mapToCurrencyCode(account, balances);

        // then
        assertThat(currencyCode).isEqualTo(CurrencyCode.XXX);
    }

    @ParameterizedTest
    @CsvSource({"XPCD,EXPECTED", "CLBD,CLOSING_BOOKED", "OTHR,AUTHORISED", "VALU,FORWARD_AVAILABLE"})
    void shouldMapToBalanceType(String inputBalanceType, String expectedBalanceType) {
        // given
        StetBalanceType balanceStatus = StetBalanceType.valueOf(inputBalanceType);

        // when
        BalanceType balanceType = accountMapper.mapToBalanceType(balanceStatus);

        // then
        assertThat(balanceType).isEqualTo(BalanceType.valueOf(expectedBalanceType));
    }

    @Test
    void shouldMapToAccountReferenceDTOs() {
        // given
        StetAccountDTO account = createStetAccountDTO(ACCOUNT_IBAN, EUR);

        // when
        List<AccountReferenceDTO> accountReferenceDTOs = accountMapper.mapToAccountReferenceDTOs(account);

        // then
        assertThat(accountReferenceDTOs).hasSize(1);

        AccountReferenceDTO accountReferenceDTO = accountReferenceDTOs.get(0);
        assertThat(accountReferenceDTO.getValue()).isEqualTo(ACCOUNT_IBAN);
        assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
    }

    @Test
    void shouldMapToAccountReferenceDTOsEvenIfIbanContainsWhitespaces() {
        // given
        StetAccountDTO account = createStetAccountDTO("FR4 2300 0300030 632774 8225F14", EUR);

        // when
        List<AccountReferenceDTO> accountReferenceDTOs = accountMapper.mapToAccountReferenceDTOs(account);

        // then
        assertThat(accountReferenceDTOs).hasSize(1);

        AccountReferenceDTO accountReferenceDTO = accountReferenceDTOs.get(0);
        assertThat(accountReferenceDTO.getValue()).isEqualTo(ACCOUNT_IBAN);
        assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
    }

    @ParameterizedTest
    @CsvSource({"CACC,CURRENT_ACCOUNT", "CARD,CREDIT_CARD"})
    void shouldMapToAccountType(String inputStetAccountType, String expectedAccountType) {
        // given
        StetAccountType cashAccountType = StetAccountType.valueOf(inputStetAccountType);

        // when
        AccountType accountType = accountMapper.mapToAccountType(cashAccountType);

        // then
        assertThat(accountType).isEqualTo(AccountType.valueOf(expectedAccountType));
    }

    @Test
    void shouldMapToProviderCreditCardDTO() {
        // given
        StetAccountDTO accountDTO = createStetAccountDTO(ACCOUNT_IBAN, CARD, EUR);
        BigDecimal balanceAmount = new BigDecimal("100.52");

        // when
        ProviderCreditCardDTO providerCreditCardDTO = accountMapper.mapToProviderCreditCardDTO(accountDTO, balanceAmount);

        // then
        assertThat(providerCreditCardDTO.getAvailableCreditAmount()).isEqualTo(balanceAmount);
    }

    @ParameterizedTest
    @CsvSource({"PRIV,PRIVATE", "ORGA,CORPORATE"})
    void shouldMapUsageType(String inputStetAccountUsage, String expectedUsageType) {
        // given
        StetAccountUsage accountUsage = StetAccountUsage.valueOf(inputStetAccountUsage);

        // when
        UsageType usageType = accountMapper.mapToUsageType(accountUsage);

        // then
        assertThat(usageType).isEqualTo(UsageType.valueOf(expectedUsageType));
    }

    @Test
    void shouldMapToProviderAccountNumberDTO() {
        // given
        StetAccountDTO accountDTO = createStetAccountDTO(ACCOUNT_IBAN, EUR);

        // when
        ProviderAccountNumberDTO providerAccountNumberDTO = accountMapper.mapToProviderAccountNumberDTO(accountDTO, accountDTO.getName());

        // then
        assertThat(providerAccountNumberDTO.getScheme()).isEqualTo(IBAN);
        assertThat(providerAccountNumberDTO.getIdentification()).isEqualTo(accountDTO.getIban());
        assertThat(providerAccountNumberDTO.getHolderName()).isEqualTo(accountDTO.getName());
    }

    @Test
    void shouldNotMapToProviderAccountNumberDTODueToMissingIban() {
        // given
        String iban = "";
        StetAccountDTO accountDTO = createStetAccountDTO(iban, EUR);

        // when
        ProviderAccountNumberDTO providerAccountNumberDTO = accountMapper.mapToProviderAccountNumberDTO(accountDTO, accountDTO.getName());

        // then
        assertThat(providerAccountNumberDTO).isNull();
    }

    @Test
    void shouldMapToBalanceDTOs() {
        // given
        StetBalanceDTO interimAvailableBalance = createStetBalanceDTO(XPCD, "10.10", EUR);
        StetBalanceDTO closingBookedBalance = createStetBalanceDTO(CLBD, "20.20", EUR);

        // when
        List<BalanceDTO> balanceDTOs = accountMapper.mapToBalanceDTOs(List.of(interimAvailableBalance, closingBookedBalance));

        // then
        assertThat(balanceDTOs).hasSize(2);

        BalanceDTO interimAvailableBalanceDTO = balanceDTOs.get(0);
        assertThat(interimAvailableBalanceDTO.getBalanceType()).isEqualTo(BalanceType.EXPECTED);
        assertThat(interimAvailableBalanceDTO.getBalanceAmount().getAmount()).isEqualTo("10.10");
        assertThat(interimAvailableBalanceDTO.getBalanceAmount().getCurrency()).isEqualTo(EUR);

        BalanceDTO closingBookedBalanceDTO = balanceDTOs.get(1);
        assertThat(closingBookedBalanceDTO.getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);
        assertThat(closingBookedBalanceDTO.getBalanceAmount().getAmount()).isEqualTo("20.20");
        assertThat(closingBookedBalanceDTO.getBalanceAmount().getCurrency()).isEqualTo(EUR);
    }

    @Test
    void shouldReturnBalanceAmountByPreferredBalanceType() {
        // given
        List<StetBalanceType> balanceTypes = Collections.singletonList(XPCD);
        List<StetBalanceDTO> balanceDTOs = List.of(
                createStetBalanceDTO(OTHR, "10.10", EUR),
                createStetBalanceDTO(CLBD, "20.20", EUR),
                createStetBalanceDTO(XPCD, "30.30", EUR));

        // when
        BigDecimal balanceAmount = accountMapper.getBalanceAmount(balanceDTOs, balanceTypes);

        // then
        assertThat(balanceAmount).isEqualTo("30.30");
    }

    @Test
    void shouldReturnBalanceAmountByPreferredBalanceTypeInSpecificOrder() {
        // given
        List<StetBalanceType> balanceTypes = List.of(XPCD, CLBD, OTHR);
        List<StetBalanceDTO> balanceDTOs = List.of(
                createStetBalanceDTO(OTHR, "40.40", EUR),
                createStetBalanceDTO(CLBD, "50.50", EUR));

        // when
        BigDecimal balanceAmount = accountMapper.getBalanceAmount(balanceDTOs, balanceTypes);

        // then
        assertThat(balanceAmount).isEqualTo("50.50");
    }

    @Test
    void shouldMapHolderNameBasedOnAccountName() {
        // given
        StetAccountDTO accountDTO = createStetAccountDTO(ACCOUNT_IBAN, EUR);

        // when
        String holderName = accountMapper.mapToHolderName(accountDTO);

        // then
        assertThat(holderName).isEqualTo(accountDTO.getName());
    }

    @Test
    void shouldReturnPreferredAvailableBalanceTypes() {
        // when
        List<StetBalanceType> preferredAvailableBalanceTypes = accountMapper.getPreferredAvailableBalanceTypes();

        // then
        assertThat(preferredAvailableBalanceTypes).containsExactly(XPCD);
    }

    @Test
    void shouldReturnPreferredCurrentBalanceTypes() {
        // when
        List<StetBalanceType> preferredCurrentBalanceTypes = accountMapper.getPreferredCurrentBalanceTypes();

        // then
        assertThat(preferredCurrentBalanceTypes).containsExactly(CLBD);
    }

    @Test
    void shouldReturnPreferredCardBalanceTypes() {
        // when
        List<StetBalanceType> preferredCardBalanceTypes = accountMapper.getPreferredCardBalanceType();

        // then
        assertThat(preferredCardBalanceTypes).containsExactly(OTHR, XPCD);
    }

    private StetAccountDTO createStetAccountDTO(String iban, CurrencyCode currency) {
        return createStetAccountDTO(iban, StetAccountType.CACC, currency);
    }

    private StetAccountDTO createStetAccountDTO(String iban, StetAccountType purpose, CurrencyCode currency) {
        return TestStetAccountDTO.builder()
                .resourceId("ResourceId")
                .iban(iban)
                .currency(currency)
                .bicFi("MUTXEWXM")
                .type(purpose)
                .details("Details")
                .name("AcocuntName")
                .usage(StetAccountUsage.PRIV)
                .linkedAccount("LinkedAccounts")
                .build();
    }

    private StetBalanceDTO createStetBalanceDTO(StetBalanceType balanceStatus, CurrencyCode currency) {
        return createStetBalanceDTO(balanceStatus, "100.50", currency);
    }

    private StetBalanceDTO createStetBalanceDTO(StetBalanceType balanceStatus, String amount, CurrencyCode currency) {
        return TestStetBalanceDTO.builder()
                .amount(new BigDecimal(amount))
                .currency(currency)
                .type(balanceStatus)
                .lastChangeDateTime(OffsetDateTime.now())
                .lastCommittedTransaction("LastCommittedTransaction")
                .name("BalanceName")
                .referenceDate(OffsetDateTime.now())
                .build();
    }

    private Consumer<ProviderAccountDTO> validateAccountDTO(StetAccountDTO accountDTO) {
        return (providerAccountDTO) -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountDTO.getResourceId());
            assertThat(providerAccountDTO.getBic()).isEqualTo(accountDTO.getBicFi());
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("100.55");
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("267.82");
            assertThat(providerAccountDTO.getName()).isEqualTo(accountDTO.getName());
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isNotNull();
            assertThat(providerAccountDTO.getLinkedAccount()).isEqualTo(accountDTO.getLinkedAccount());
            assertThat(providerAccountDTO.getTransactions()).isEmpty();
            assertThat(providerAccountDTO.getDirectDebits()).isNull();
            assertThat(providerAccountDTO.getStandingOrders()).isNull();
            assertThat(providerAccountDTO.getAccountMaskedIdentification()).isNull();
            assertThat(providerAccountDTO.getClosed()).isNull();

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(accountNumberDTO.getScheme()).isEqualTo(IBAN);
            assertThat(accountNumberDTO.getIdentification()).isEqualTo(accountDTO.getIban());
            assertThat(accountNumberDTO.getHolderName()).isEqualTo(accountDTO.getName());

            if (CARD.equals(accountDTO.getType())) {
                ProviderCreditCardDTO creditCardData = providerAccountDTO.getCreditCardData();
                assertThat(creditCardData.getAvailableCreditAmount()).isEqualTo(providerAccountDTO.getAvailableBalance());
            }
            validateExtendedAccountDTO(accountDTO).accept(providerAccountDTO.getExtendedAccount());
        };
    }

    private Consumer<ExtendedAccountDTO> validateExtendedAccountDTO(StetAccountDTO accountDTO) {
        return (extendedAccountDTO) -> {
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountDTO.getResourceId());
            assertThat(extendedAccountDTO.getUsage()).isEqualTo(UsageType.PRIVATE);
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(extendedAccountDTO.getBic()).isEqualTo(accountDTO.getBicFi());
            assertThat(extendedAccountDTO.getDetails()).isEqualTo(accountDTO.getDetails());
            assertThat(extendedAccountDTO.getName()).isEqualTo(accountDTO.getName());
            assertThat(extendedAccountDTO.getProduct()).isEqualTo(accountDTO.getProduct());
            assertThat(extendedAccountDTO.getLinkedAccounts()).isEqualTo(accountDTO.getLinkedAccount());
            assertThat(extendedAccountDTO.getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);
            assertThat(extendedAccountDTO.getStatus()).isEqualTo(Status.ENABLED);

            List<AccountReferenceDTO> accountReferenceDTOs = extendedAccountDTO.getAccountReferences();
            assertThat(accountReferenceDTOs).hasSize(1);

            AccountReferenceDTO accountReferenceDTO = accountReferenceDTOs.get(0);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(accountDTO.getIban());
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);

            List<BalanceDTO> balanceDTOs = extendedAccountDTO.getBalances();
            assertThat(balanceDTOs).hasSize(2);

            BalanceDTO firstBalanceDTO = balanceDTOs.get(0);
            assertThat(firstBalanceDTO.getBalanceAmount().getAmount()).isEqualTo("100.55");
            assertThat(firstBalanceDTO.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(firstBalanceDTO.getBalanceType()).isEqualTo(BalanceType.EXPECTED);
            assertThat(firstBalanceDTO.getLastChangeDateTime()).isNotNull();
            assertThat(firstBalanceDTO.getLastCommittedTransaction()).isNotNull();

            BalanceDTO secondBalanceDTO = balanceDTOs.get(1);
            assertThat(secondBalanceDTO.getBalanceAmount().getAmount()).isEqualTo("267.82");
            assertThat(secondBalanceDTO.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(secondBalanceDTO.getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);
            assertThat(secondBalanceDTO.getLastChangeDateTime()).isNotNull();
            assertThat(secondBalanceDTO.getLastCommittedTransaction()).isNotNull();
        };
    }
}
