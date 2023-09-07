package com.yolt.providers.starlingbank.common.mapper;

import com.yolt.providers.starlingbank.common.model.*;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;

import static nl.ing.lovebird.extendeddata.account.BalanceType.EXPECTED;
import static nl.ing.lovebird.extendeddata.account.BalanceType.INTERIM_AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

public class ExtendedModelMapperV3Test {

    private static final BigDecimal AMOUNT_1 = new BigDecimal("1");
    private static final BigDecimal AMOUNT_10 = new BigDecimal("10.00");
    private static final BigDecimal AMOUNT_100 = new BigDecimal("100");
    private static final BigDecimal AMOUNT_1000 = new BigDecimal("1000");
    private static final String CURRENCY_EUR = "EUR";
    private static final String CREATED = "2017-07-05T18:27:02.335Z";
    private static final String CATEGORY = "category";
    private static final String TRANSACTION_INFORMATION = "transactionInformation";
    private static final String ACCOUNT_UID = "b0b20c9d--42f1-a7d0-e70d4538e0d9";
    private static final String ACCOUNT_IDENTIFIER = "01234567";
    private static final String BANK_IDENTIFIER = "608371";
    private static final String TRANSACTION_UID = "43b4c184-4e31-4ef0-bc71-cb460c10e26d";
    private static final String IBAN = "GB50SRLG60837112345678";
    private static final String BIC = "SRLGGB2L";
    private static final String STARLING_ACCOUNT_NAME = "Starling Bank";
    private static final String COUNTER_PARTY_NAME = "Endeavour Morse" ;
    private static final String SUB_ENTITY_ID = "666665" ;
    private static final String SUB_ENTITY_SUB_ID = "12344321" ;
    private static final String SOURCE = "CARD PAYMENT" ;
    private static final String SOURCE_SUBTYPE = "CONTACTLESS" ;

    @Test
    public void shouldReturnExtendedAccountForMapToExtendedModelAccountWithCorrectData() {
        // given
        AccountV2 accountsResponse = prepareValidAccount();
        AccountIdentifiersV2 accountIdentifiers = prepareAccountIdentifiers();
        BalancesResponseV2 balancesResponse = prepareValidBalances();

        // when
        ExtendedAccountDTO account = ExtendedModelMapperV3
                .mapToExtendedModelAccount(accountsResponse, accountIdentifiers, balancesResponse);

        // then
        assertThat(account.getName()).isEqualTo(STARLING_ACCOUNT_NAME);
        assertThat(account.getBic()).isEqualTo(BIC);
        assertThat(account.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account.getAccountReferences()).hasSize(2);

        AccountReferenceDTO accountReferenceDTO = account.getAccountReferences().get(0);
        assertThat(accountReferenceDTO.getValue()).isEqualTo(IBAN);

        List<BalanceDTO> balances = account.getBalances();
        assertThat(balances).hasSize(2);
        assertThat(balances.get(0).getBalanceType()).isEqualTo(EXPECTED);
        assertThat(balances.get(1).getBalanceType()).isEqualTo(INTERIM_AVAILABLE);
    }

    @Test
    public void shouldReturnExtendedTransactionForMapToExtendedModelTransactionWithCorrectData() {
        // given
        FeedItemV2 transaction = prepareTransaction();

        // when
        ExtendedTransactionDTO extendedTransaction = ExtendedModelMapperV3
                .mapToExtendedModelTransaction(transaction);

        // then
        assertThat(extendedTransaction).isNotNull();
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo(TRANSACTION_INFORMATION);
        assertThat(extendedTransaction.getBookingDate().getZone()).isEqualTo(ZoneId.of("Europe/London"));
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo(AMOUNT_10);
        assertThat(extendedTransaction.getProprietaryBankTransactionCode()).isEqualTo(String.format("%s %s", SOURCE, SOURCE_SUBTYPE));
    }

    private BalancesResponseV2 prepareValidBalances() {

        return BalancesResponseV2.builder()
                .availableToSpend(new CurrencyAndAmountV2(CURRENCY_EUR, AMOUNT_1000))
                .effectiveBalance(new CurrencyAndAmountV2(CURRENCY_EUR, AMOUNT_1))
                .clearedBalance(new CurrencyAndAmountV2(CURRENCY_EUR, AMOUNT_100))
                .pendingTransactions(new CurrencyAndAmountV2(CURRENCY_EUR, AMOUNT_1000))
                .acceptedOverdraft(new CurrencyAndAmountV2(CURRENCY_EUR, AMOUNT_1))
                .amount(new CurrencyAndAmountV2(CURRENCY_EUR, AMOUNT_100))
                .build();
    }

    private AccountV2 prepareValidAccount() {
        return AccountV2.builder()
                .currency(CURRENCY_EUR)
                .accountUid(ACCOUNT_UID)
                .defaultCategory(CATEGORY)
                .createdAt(CREATED)
                .build();
    }

    private AccountIdentifiersV2 prepareAccountIdentifiers() {
        return AccountIdentifiersV2.builder()
                .accountIdentifier(ACCOUNT_IDENTIFIER)
                .bankIdentifier(BANK_IDENTIFIER)
                .iban(IBAN)
                .bic(BIC)
                .build();
    }

    private FeedItemV2 prepareTransaction() {
        return FeedItemV2.builder()
                .feedItemUid(TRANSACTION_UID)
                .amount(new CurrencyAndAmountV2(CURRENCY_EUR, AMOUNT_1000))
                .direction(FeedItemDirection.IN)
                .transactionTime(CREATED)
                .status(TransactionStatusV2.PENDING)
                .userNote(TRANSACTION_INFORMATION)
                .counterPartyName(COUNTER_PARTY_NAME)
                .counterPartySubEntityIdentifier(SUB_ENTITY_ID)
                .counterPartySubEntitySubIdentifier(SUB_ENTITY_SUB_ID)
                .source(SOURCE)
                .sourceSubType(SOURCE_SUBTYPE)
                .build();
    }
}