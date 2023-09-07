package com.yolt.providers.stet.bnpparibasgroup.mapper;

import com.yolt.providers.stet.bnpparibasgroup.common.mapper.BnpParibasGroupDataTimeSupplier;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupAccountMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupBalanceMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupTransactionMapper;
import com.yolt.providers.stet.bnpparibasgroup.hellobank.mapper.HelloBankAccountMapper;
import com.yolt.providers.stet.generic.dto.TestStetAccountDTO;
import com.yolt.providers.stet.generic.dto.TestStetBalanceDTO;
import com.yolt.providers.stet.generic.dto.TestStetTransactionDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountUsage;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionIndicator;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.yolt.providers.stet.generic.dto.account.StetAccountType.CACC;
import static com.yolt.providers.stet.generic.dto.account.StetAccountUsage.ORGA;
import static com.yolt.providers.stet.generic.dto.account.StetAccountUsage.PRIV;
import static com.yolt.providers.stet.generic.dto.balance.StetBalanceType.*;
import static com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus.BOOK;
import static com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus.PDNG;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static nl.ing.lovebird.extendeddata.account.BalanceType.CLOSING_BOOKED;
import static nl.ing.lovebird.extendeddata.account.BalanceType.INTERIM_AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BnpParibasGroupAccountMapperTest {

    private static final String EXTERNAL_ACCOUNT_ID = "12";
    private static final String ACCOUNT_NAME = "Account name";
    private static final String IBAN = "FR5817569000505918431515C55";
    private static final BigDecimal FUTURE_AMOUNT = BigDecimal.valueOf(1200.20f);
    private static final BigDecimal INSTANT_AMOUNT = BigDecimal.valueOf(1500.20f);
    private static final BigDecimal ACCOUNTING_AMOUNT = BigDecimal.valueOf(175.50f);
    private static final String REMITTANCE_INFORMATION = "SEPA CREDIT TRANSFER from PSD2Company";
    private static final LocalDate LOCAL_DATE = LocalDate.of(2018, 2, 13);
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(LOCAL_DATE, LocalTime.of(8, 0), ZoneId.of("Europe/Paris").getRules().getOffset(LocalDateTime.now()));
    private static final String SUPPORTED_CURRENCY = "EUR";
    private static final CurrencyCode CURRENCY_CODE = CurrencyCode.valueOf(SUPPORTED_CURRENCY);
    private static final DateTimeSupplier dateTimeSupplier = new BnpParibasGroupDataTimeSupplier(Clock.systemUTC(), ZoneId.of("Europe/Paris"));
    private static final BnpParibasGroupBalanceMapper balanceMapper = new BnpParibasGroupBalanceMapper();
    private static final BnpParibasGroupAccountMapper bnpParibasGroupAccountMapper = new BnpParibasGroupAccountMapper(dateTimeSupplier, balanceMapper);
    private static final HelloBankAccountMapper helloBankAccountMapper = new HelloBankAccountMapper(dateTimeSupplier, balanceMapper);
    private final StetBalanceDTO ACCOUNTING_BALANCE = createBalanceResource(CLBD, ACCOUNTING_AMOUNT);
    private final StetBalanceDTO INSTANT_BALANCE = createBalanceResource(XPCD, INSTANT_AMOUNT);
    private final StetBalanceDTO FUTURE_BALANCE = createBalanceResource(OTHR, FUTURE_AMOUNT);

    private final List<StetBalanceDTO> BALANCES_WITHOUT_ACCOUNTING = List.of(INSTANT_BALANCE, FUTURE_BALANCE);
    public List<StetBalanceDTO> BALANCES_WITH_ACCOUNTING = List.of(ACCOUNTING_BALANCE, INSTANT_BALANCE, FUTURE_BALANCE);

    private Stream<Arguments> getMappersWithParameters() {
        return Stream.of(
                Arguments.of(bnpParibasGroupAccountMapper, BALANCES_WITH_ACCOUNTING, ACCOUNTING_AMOUNT, ACCOUNTING_AMOUNT, CLOSING_BOOKED, 2),
                Arguments.of(bnpParibasGroupAccountMapper, BALANCES_WITHOUT_ACCOUNTING, FUTURE_AMOUNT, INSTANT_AMOUNT, INTERIM_AVAILABLE, 1),
                Arguments.of(helloBankAccountMapper, BALANCES_WITH_ACCOUNTING, ACCOUNTING_AMOUNT, ACCOUNTING_AMOUNT, CLOSING_BOOKED, 2),
                Arguments.of(helloBankAccountMapper, BALANCES_WITHOUT_ACCOUNTING, INSTANT_AMOUNT, INSTANT_AMOUNT, INTERIM_AVAILABLE, 1)
        );
    }

    private Stream<BnpParibasGroupAccountMapper> getAccountMappers() {
        return Stream.of(bnpParibasGroupAccountMapper, helloBankAccountMapper);
    }

    @ParameterizedTest
    @MethodSource("getMappersWithParameters")
    public void shouldCorrectlyMapToProviderAccountDTO(BnpParibasGroupAccountMapper accountMapper,
                                                       List<StetBalanceDTO> balanceResources, BigDecimal expectedAmount,
                                                       BigDecimal expectedAmountInExtendedAccountDTO, BalanceType expectedBalanceType,
                                                       int expectedSize) {
        // given
        StetAccountDTO accountResource = createAccountResource(PRIV);

        List<ProviderTransactionDTO> transactionDTOs = new ArrayList<>();
        transactionDTOs.addAll(createProviderTransactionDTO(BigDecimal.valueOf(10.10), StetTransactionIndicator.CRDT, BOOK));
        transactionDTOs.addAll(createProviderTransactionDTO(BigDecimal.valueOf(20.20), StetTransactionIndicator.DBIT, PDNG));

        // when
        ProviderAccountDTO accountDTO = accountMapper.mapToProviderAccountDTO(
                accountResource, balanceResources, transactionDTOs);

        // then
        // Validate account
        assertThat(accountDTO.getAccountId()).isEqualTo(EXTERNAL_ACCOUNT_ID);
        assertThat(accountDTO.getAccountNumber().getIdentification()).isEqualTo(IBAN);
        assertThat(accountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(accountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(accountDTO.getName()).isEqualTo(ACCOUNT_NAME);
        assertThat(accountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(accountDTO.getCurrentBalance()).usingComparator(BigDecimal::compareTo).isEqualTo(expectedAmount);
        assertThat(accountDTO.getAvailableBalance()).usingComparator(BigDecimal::compareTo).isEqualTo(FUTURE_AMOUNT);

        // Validate extended account
        ExtendedAccountDTO extendedAccountDTO = accountDTO.getExtendedAccount();
        assertThat(extendedAccountDTO.getResourceId()).isEqualTo(EXTERNAL_ACCOUNT_ID);
        assertThat(extendedAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedAccountDTO.getName()).isEqualTo(ACCOUNT_NAME);
        assertThat(extendedAccountDTO.getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);
        assertThat(extendedAccountDTO.getStatus()).isEqualTo(Status.ENABLED);
        assertThat(extendedAccountDTO.getUsage()).isEqualTo(UsageType.PRIVATE);

        // Validate balances from extended account
        List<BalanceDTO> balanceDTOs = extendedAccountDTO.getBalances();
        assertThat(balanceDTOs).hasSize(expectedSize);

        // Validate current balance from extended account
        BalanceDTO currentBalanceDTO = balanceDTOs.get(0);
        assertThat(currentBalanceDTO.getBalanceType()).isEqualTo(expectedBalanceType);
        assertThat(currentBalanceDTO.getBalanceAmount().getAmount()).usingComparator(BigDecimal::compareTo)
                .isEqualTo(expectedAmountInExtendedAccountDTO);
        assertThat(currentBalanceDTO.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);

        // Validate account references from extended account
        List<AccountReferenceDTO> accountReferences = extendedAccountDTO.getAccountReferences();
        assertThat(accountReferences.get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(accountReferences.get(0).getValue()).isEqualTo(IBAN);

        List<ProviderTransactionDTO> providerTransactionDTOs = accountDTO.getTransactions();
        assertThat(providerTransactionDTOs).hasSize(2);

        accountDTO.validate();
    }

    @ParameterizedTest
    @MethodSource("getAccountMappers")
    public void shouldMapToCorporateType(BnpParibasGroupAccountMapper accountMapper) {
        // given
        StetAccountDTO stetAccountDTO = createAccountResource(ORGA);

        // when
        ProviderAccountDTO account = accountMapper.mapToProviderAccountDTO(stetAccountDTO, emptyList(), emptyList());
        //then
        assertThat(account.getExtendedAccount().getUsage()).isEqualTo(UsageType.CORPORATE);
    }

    @ParameterizedTest
    @MethodSource("getAccountMappers")
    public void shouldMapToProviderAccountDTOWithForEverySingleBalanceResource(BnpParibasGroupAccountMapper accountMapper) {
        // given
        for (StetBalanceType balanceType : StetBalanceType.values()) {
            StetBalanceDTO balanceDTO = createBalanceResource(balanceType, FUTURE_AMOUNT);

            // when
            ProviderAccountDTO accountDTO = accountMapper.mapToProviderAccountDTO(
                    createAccountResource(PRIV), singletonList(balanceDTO), emptyList());

            // then
            assertThat(accountDTO.getCurrentBalance()).isEqualTo(FUTURE_AMOUNT);
            assertThat(accountDTO.getAvailableBalance()).isEqualTo(FUTURE_AMOUNT);
        }
    }

    @ParameterizedTest
    @MethodSource("getAccountMappers")
    public void shouldMapToProviderAccountDTOWithPreferredBalanceResource(BnpParibasGroupAccountMapper accountMapper) {
        // given
        List<StetBalanceDTO> balanceResources = List.of(
                createBalanceResource(CLBD, BigDecimal.valueOf(100.00f)),
                createBalanceResource(XPCD, BigDecimal.valueOf(200.00f)),
                createBalanceResource(OTHR, BigDecimal.valueOf(300.00f)));

        // when
        ProviderAccountDTO accountDTO = accountMapper.mapToProviderAccountDTO(
                createAccountResource(PRIV), balanceResources, emptyList());

        // then
        assertThat(accountDTO.getCurrentBalance()).isEqualTo("100.0");
        assertThat(accountDTO.getAvailableBalance()).isEqualTo("300.0");
    }

    private StetAccountDTO createAccountResource(StetAccountUsage usage) {
        String accountIdentification = EXTERNAL_ACCOUNT_ID;

        return TestStetAccountDTO.builder()
                .resourceId(EXTERNAL_ACCOUNT_ID)
                .resourceId(accountIdentification)
                .currency(CURRENCY_CODE)
                .name(ACCOUNT_NAME)
                .usage(usage)
                .iban(IBAN)
                .type(CACC)
                .build();
    }

    private StetBalanceDTO createBalanceResource(StetBalanceType balanceType, BigDecimal amount) {
        return TestStetBalanceDTO.builder()
                .name("Solde comptable au 12/01/2017")
                .amount(amount)
                .currency(CURRENCY_CODE)
                .type(balanceType)
                .referenceDate(OffsetDateTime.of(LocalDateTime
                                .of(2019, 7, 1, 0, 0),
                        ZoneOffset.ofHoursMinutes(0, 0)))
                .build();
    }

    private List<ProviderTransactionDTO> createProviderTransactionDTO(BigDecimal amount, StetTransactionIndicator stetTransactionIndicator,
                                                                      StetTransactionStatus transactionStatus) {
        List<String> remittanceInformation = new ArrayList<>();
        remittanceInformation.add(REMITTANCE_INFORMATION);

        return new BnpParibasGroupTransactionMapper(dateTimeSupplier).mapToProviderTransactionDTOs(List.of(
                TestStetTransactionDTO.builder()
                        .resourceId(EXTERNAL_ACCOUNT_ID)
                        .entryReference("AF5T2")
                        .amount(amount)
                        .transactionIndicator(stetTransactionIndicator)
                        .status(transactionStatus)
                        .bookingDate(OFFSET_DATE_TIME)
                        .transactionDate(OFFSET_DATE_TIME)
                        .unstructuredRemittanceInformation(remittanceInformation)
                        .build())
        );
    }

}



