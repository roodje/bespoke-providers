package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.extendedtransactionmapper.HsbcGroupBalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.ing.lovebird.extendeddata.account.BalanceType.*;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test class verifies logic responsible for mapping data to extended model.
 * Covered flows:
 * - mapping data to extended account model
 * - mapping data to extended account model when some data is missing
 * - mapping data to extended transaction model
 * - mapping data to extended transaction model when some data is missing
 * <p>
 */
class HsbcGroupExtendedModelMapperV5Test {

    private static final String AMOUNT_1 = "1";
    private static final String AMOUNT_100 = "100";
    private static final String AMOUNT_1000 = "1,000";
    private static final String CURRENCY_PLN = "PLN";
    private static final String CURRENCY_EUR = "EUR";
    private static final String CURRENCY_USD = "USD";
    private static final String IDENTIFICATION = "Identification";
    private static final String NAME = "name";
    private static final String PROPRIETARY_CODE = "proprietaryCode";
    private static final String ISSUER = "issuer";
    private static final String TRANSACTION_INFORMATION = "transactionInformation";
    private static final String TRANSACTION_REFERENCE = "transactionReference";

    private DefaultExtendedAccountMapper accountMapper = new DefaultExtendedAccountMapper(
            new DefaultAccountReferenceTypeMapper(),
            new DefaultCurrencyMapper(),
            new DefaultExtendedBalancesMapper(
                    new DefaultBalanceAmountMapper(
                            new DefaultCurrencyMapper(),
                            new DefaultBalanceMapper()),
                    new DefaultBalanceTypeMapper(),
                    ZoneId.of("Europe/London")));
    private DefaultExtendedTransactionMapper transactionMapper = new DefaultExtendedTransactionMapper(
            new DefaultAccountReferenceTypeMapper(),
            new DefaultTransactionStatusMapper(),
            new DefaultBalanceAmountMapper(new DefaultCurrencyMapper(), new HsbcGroupBalanceMapper()),
            false,
            ZoneId.of("Europe/London"));

    @Test
    void shouldMapToExtendedModelAccount() {
        // given
        OBAccount6 account = new OBAccount6()
                .currency(CURRENCY_EUR)
                .account(Collections.singletonList(new OBAccount4Account()
                        .identification(IDENTIFICATION)
                        .schemeName("UK.OBIE.IBAN")));

        // when
        ExtendedAccountDTO result = accountMapper.mapToExtendedModelAccount(account, NAME, prepareValidBalances());

        // then
        assertThat(result.getAccountReferences()).as("IBAN identification should be mapped properly").isNotEmpty();
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getAccountReferences().get(0).getValue()).isEqualTo(IDENTIFICATION);
        assertThat(result.getCurrency().name()).isEqualTo(CURRENCY_EUR);
        assertValidBalances(result.getBalances());
    }

    @Test
    void shouldMapToExtendedModelAccountWhenMissingData() {
        // given
        OBAccount6 account = new OBAccount6()
                .currency(CURRENCY_PLN)
                .account(Collections.singletonList(new OBAccount4Account()
                        .identification(IDENTIFICATION)
                        .name(NAME)
                        .schemeName("SortCodeAccountNumber")));

        // when
        ExtendedAccountDTO result = accountMapper.mapToExtendedModelAccount(account, NAME, getBalancesWithoutType());

        // then
        assertThat(result.getCurrency().name()).isEqualTo(CURRENCY_PLN);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getAccountReferences()).as("SORTCODEACCOUNTNUMBER identification should not be mapped").isEmpty();
        assertThat(result.getBalances()).as("Balances without type should not be mapped at all").isEmpty();
    }

    @Test
    void shouldMapToExtendedModelTransaction() {
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
        ExtendedTransactionDTO result = transactionMapper.apply(transaction);

        // then
        assertThat(result).as("Transaction should be mapped without error").isNotNull();
        assertThat(result.getStatus()).isEqualTo(BOOKED);
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo(TRANSACTION_INFORMATION);
        assertThat(result.getProprietaryBankTransactionCode()).isEqualTo(PROPRIETARY_CODE);
        assertThat(result.getEntryReference()).isEqualTo(TRANSACTION_REFERENCE);
        assertThat(result.getTransactionAmount().getAmount()).isNegative();
    }

    @Test
    void shouldMapToExtendedModelTransactionWhenMissingData() {
        // given
        OBTransaction6 transaction = new OBTransaction6()
                .bookingDateTime(OffsetDateTime.now().minusDays(1).toString())
                .amount(new OBActiveOrHistoricCurrencyAndAmount9()
                        .amount(AMOUNT_1000)
                        .currency(CURRENCY_PLN));

        // when
        ExtendedTransactionDTO result = transactionMapper.apply(transaction);

        // then
        assertThat(result).as("Transaction should be mapped without error").isNotNull();
    }

    private void assertValidBalances(final List<BalanceDTO> balances) {
        assertThat(balances.get(0).getBalanceType()).isEqualTo(CLOSING_BOOKED);
        assertThat(balances.get(1).getBalanceType()).isEqualTo(INTERIM_AVAILABLE);
        assertThat(balances.get(2).getBalanceType()).isEqualTo(OPENING_BOOKED);
        assertThat(balances).hasSize(3);
        assertThat(balances.get(0).getBalanceAmount().getAmount()).isPositive();
        assertThat(balances.get(1).getBalanceAmount().getAmount()).isNegative();
        balances.forEach(balanceDTO -> assertThat(balanceDTO.getBalanceType()).as("All passed balance types should be mapped").isNotNull());
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
                        .amount(AMOUNT_100))
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
                .type(OBBalanceType1Code.INFORMATION)
                .amount(new OBReadBalance1DataAmount()
                        .currency(CURRENCY_USD)
                        .amount(AMOUNT_100)));
        return balances;
    }

    private List<OBReadBalance1DataBalance> getBalancesWithoutType() {
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
                .amount(new OBReadBalance1DataAmount()
                        .currency(CURRENCY_USD)
                        .amount(AMOUNT_100)));
        return balances;
    }
}
