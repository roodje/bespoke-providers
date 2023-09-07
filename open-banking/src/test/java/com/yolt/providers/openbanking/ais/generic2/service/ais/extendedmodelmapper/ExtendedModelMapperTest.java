package com.yolt.providers.openbanking.ais.generic2.service.ais.extendedmodelmapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.UsageType;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static nl.ing.lovebird.extendeddata.account.BalanceType.*;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ExtendedModelMapperTest {

    private static final String AMOUNT_1 = "1";
    private static final String AMOUNT_100 = "100";
    private static final String AMOUNT_1000 = "1000";
    private static final String CURRENCY_PLN = "PLN";
    private static final String CURRENCY_EUR = "EUR";
    private static final String CURRENCY_USD = "USD";
    private static final String IDENTIFICATION = "Identification";
    private static final String NAME = "name";
    private static final String PROPRIETARY_CODE = "proprietaryCode";
    private static final String ISSUER = "issuer";
    private static final String TRANSACTION_INFORMATION = "transactionInformation";
    private static final String TRANSACTION_REFERENCE = "transactionReference";
    private static final String IDENTIFICATION_IBAN = "IdentificationIban";
    private static final String IDENTIFICATION_SORTCODE = "IdentificationSortCode";
    private static final String IDENTIFICATION_PAN = "11111111111111111";
    private static final String IDENTIFICATION_MASKED_PAN = "111111xxx11111111";
    private static final String NAME_IBAN = "NameIban";
    private static final String NAME_SORTCODE = "NameSortCode";
    private static final String NAME_PAN = "NamePan";
    private static final String NAME_MASKED_PAN = "NameMaskedPan";
    private static final String EXTRACTED_NAME = "extractedName";
    private static final String OB_SCHEMA_PREFIX = "UK.OBIE.";

    private final DefaultExtendedTransactionMapper extendedTransactionMapper = new DefaultExtendedTransactionMapper(
            new DefaultAccountReferenceTypeMapper(),
            new DefaultTransactionStatusMapper(),
            new DefaultBalanceAmountMapper(new DefaultCurrencyMapper(), new DefaultBalanceMapper()),
            false,
            ZoneId.of("Europe/London"));

    private final DefaultExtendedAccountMapper extendedAccountMapper =
            new DefaultExtendedAccountMapper(
                    new DefaultAccountReferenceTypeMapper(),
                    new DefaultCurrencyMapper(),
                    new DefaultExtendedBalancesMapper(new DefaultBalanceAmountMapper(
                            new DefaultCurrencyMapper(),
                            new DefaultBalanceMapper()),
                            new DefaultBalanceTypeMapper(),
                            ZoneId.of("Europe/London"))
            );

    @Test
    public void shouldReturnExtendedAccountModelForMapToExtendedModelAccountWithValidData() {
        // given
        OBAccount6 account = new OBAccount6()
                .currency(CURRENCY_EUR)
                .account(Collections.singletonList(new OBAccount4Account()
                        .identification(IDENTIFICATION)
                        .name(NAME)
                        .schemeName(OB_SCHEMA_PREFIX + "IBAN")));

        // when
        ExtendedAccountDTO result = extendedAccountMapper.mapToExtendedModelAccount(account, NAME, prepareValidBalances());

        // then
        assertThat(result.getAccountReferences())
                .withFailMessage("IBAN identification should be mapped properly")
                .isNotEmpty();
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getAccountReferences().get(0).getValue()).isEqualTo(IDENTIFICATION);
        assertThat(result.getCurrency().name()).isEqualTo(CURRENCY_EUR);
        assertValidBalances(result.getBalances());
    }

    private void assertValidBalances(final List<BalanceDTO> balances) {
        assertThat(balances).hasSize(3);
        assertThat(balances)
                .withFailMessage("All passed balance types should be mapped")
                .extracting("balanceType", BalanceType.class)
                .contains(CLOSING_BOOKED, INTERIM_AVAILABLE, OPENING_BOOKED);
        assertThat(balances.get(1).getBalanceAmount().getAmount()).isNegative();
    }

    @Test
    public void shouldReturnExtendedAccountModelWithoutAccountReferencesAndBalancesForMapToExtendedModelAccountWithMissingData() {
        // given
        OBAccount6 account = new OBAccount6()
                .currency(CURRENCY_PLN)
                .account(Collections.singletonList(new OBAccount4Account()
                        .identification(IDENTIFICATION)
                        .name(NAME)
                        .schemeName(OB_SCHEMA_PREFIX + "SORTCODEACCOUNTNUMBER")));

        // when
        ExtendedAccountDTO result = extendedAccountMapper.mapToExtendedModelAccount(account, NAME, prepareMinimumDataBalances());

        // then
        assertThat(result.getCurrency().name()).isEqualTo(CURRENCY_PLN);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getAccountReferences())
                .withFailMessage("SORTCODEACCOUNTNUMBER identification should not be mapped")
                .isEmpty();
        assertThat(result.getBalances())
                .withFailMessage("Balances without type should not be mapped at all")
                .isEmpty();
    }

    @Test
    public void shouldReturnExtendedTransactionModelForMapToExtendedModelTransactionWithValidData() {
        // given
        OBTransaction6 transaction = new OBTransaction6()
                .bookingDateTime(OffsetDateTime.now().minusDays(1).toString())
                .status(OBEntryStatus1Code.BOOKED)
                .proprietaryBankTransactionCode(new ProprietaryBankTransactionCodeStructure1()
                        .code(PROPRIETARY_CODE)
                        .issuer(ISSUER))
                .transactionInformation(TRANSACTION_INFORMATION)
                .valueDateTime(OffsetDateTime.now().toString())
                .creditDebitIndicator(OBCreditDebitCode1.DEBIT)
                .transactionReference(TRANSACTION_REFERENCE)
                .amount(new OBActiveOrHistoricCurrencyAndAmount9()
                        .amount(AMOUNT_1000)
                        .currency(CURRENCY_PLN));

        // when
        ExtendedTransactionDTO result = extendedTransactionMapper.apply(transaction);

        // then
        assertThat(result)
                .withFailMessage("Transaction should be mapped without error")
                .isNotNull();
        assertThat(result.getStatus()).isEqualTo(BOOKED);
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo(TRANSACTION_INFORMATION);
        assertThat(result.getProprietaryBankTransactionCode()).isEqualTo(PROPRIETARY_CODE);
        assertThat(result.getEntryReference()).isEqualTo(TRANSACTION_REFERENCE);
        assertThat(result.getTransactionAmount().getAmount()).isNegative();
    }

    @Test
    public void shouldReturnExtendedTransactionModelWithoutErrorForMapToExtendedModelTransactionWithMissingData() {
        // given
        OBTransaction6 transaction = new OBTransaction6()
                .amount(new OBActiveOrHistoricCurrencyAndAmount9()
                        .amount(AMOUNT_1000)
                        .currency(CURRENCY_PLN))
                .bookingDateTime(OffsetDateTime.now().toString());

        // when
        ExtendedTransactionDTO result = extendedTransactionMapper.apply(transaction);

        // then
        assertThat(result)
                .withFailMessage("Transaction should be mapped without error")
                .isNotNull();
    }

    @Test
    public void shouldReturnExtendedAccountModelWithoutDuplicatedAccountReferencesForMapToExtendedModelAccountWithDuplicatedOBAccounts() {
        // given
        OBAccount6 account = new OBAccount6()
                .currency(CURRENCY_EUR)
                .accountType(OBExternalAccountType1Code.BUSINESS)
                .account(Arrays.asList(
                        new OBAccount4Account()
                                .identification(IDENTIFICATION_IBAN)
                                .name(NAME_IBAN)
                                .schemeName(OB_SCHEMA_PREFIX + "IBAN"),
                        new OBAccount4Account()
                                .identification(IDENTIFICATION_MASKED_PAN)
                                .name(NAME_MASKED_PAN)
                                .schemeName(OB_SCHEMA_PREFIX + "PAN"),
                        new OBAccount4Account()
                                .identification(IDENTIFICATION_PAN)
                                .name(NAME_PAN)
                                .schemeName(OB_SCHEMA_PREFIX + "PAN"),
                        new OBAccount4Account()
                                .identification(IDENTIFICATION_SORTCODE)
                                .name(NAME_SORTCODE)
                                .schemeName(OB_SCHEMA_PREFIX + "SORTCODEACCOUNTNUMBER")
                ));

        // when
        ExtendedAccountDTO result = extendedAccountMapper.mapToExtendedModelAccount(account, EXTRACTED_NAME, prepareValidBalances());

        // then
        assertThat(result.getAccountReferences())
                .withFailMessage("IBAN identification should be mapped properly")
                .isNotEmpty();
        assertThat(result.getName()).isEqualTo(EXTRACTED_NAME);
        assertThat(result.getCurrency().name()).isEqualTo(CURRENCY_EUR);
        assertThat(result.getUsage()).isEqualTo(UsageType.CORPORATE);

        assertValidBalances(result.getBalances());
        assertThat(result.getAccountReferences()).extracting("type", "value")
                .contains(
                        tuple(AccountReferenceType.IBAN, IDENTIFICATION_IBAN),
                        tuple(AccountReferenceType.MASKED_PAN, IDENTIFICATION_MASKED_PAN),
                        tuple(AccountReferenceType.PAN, IDENTIFICATION_PAN)
                );
    }

    private List<OBReadBalance1DataBalance> prepareValidBalances() {
        List<OBReadBalance1DataBalance> balances = new ArrayList<>();
        balances.add(new OBReadBalance1DataBalance()
                .type(OBBalanceType1Code.CLOSINGBOOKED)
                .creditDebitIndicator(OBCreditDebitCode2.CREDIT)
                .amount(new OBReadBalance1DataAmount()
                        .currency(CURRENCY_PLN)
                        .amount(AMOUNT_1))
                .dateTime(OffsetDateTime.now().toString()));
        balances.add(new OBReadBalance1DataBalance()
                .creditDebitIndicator(OBCreditDebitCode2.DEBIT)
                .type(OBBalanceType1Code.INTERIMAVAILABLE)
                .amount(new OBReadBalance1DataAmount()
                        .currency(CURRENCY_PLN)
                        .amount(AMOUNT_100))
                .dateTime(OffsetDateTime.now().toString()));
        balances.add(new OBReadBalance1DataBalance()
                .creditDebitIndicator(OBCreditDebitCode2.CREDIT)
                .type(OBBalanceType1Code.OPENINGBOOKED)
                .amount(new OBReadBalance1DataAmount()
                        .currency(CURRENCY_PLN)
                        .amount(AMOUNT_1000))
                .dateTime(OffsetDateTime.now().toString()));
        return balances;
    }

    private List<OBReadBalance1DataBalance> prepareMinimumDataBalances() {
        List<OBReadBalance1DataBalance> balances = new ArrayList<>();
        balances.add(new OBReadBalance1DataBalance()
                .amount(new OBReadBalance1DataAmount()
                        .currency(CURRENCY_PLN)
                        .amount(AMOUNT_1))
                .dateTime(OffsetDateTime.now().toString()));
        balances.add(new OBReadBalance1DataBalance()
                .creditDebitIndicator(OBCreditDebitCode2.CREDIT)
                .amount(new OBReadBalance1DataAmount()
                        .currency(CURRENCY_EUR)
                        .amount(AMOUNT_100)));
        balances.add(new OBReadBalance1DataBalance()
                .type(null)
                .amount(new OBReadBalance1DataAmount()
                        .currency(CURRENCY_USD)
                        .amount(AMOUNT_1000)));
        return balances;
    }
}
