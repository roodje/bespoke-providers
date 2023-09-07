package com.yolt.providers.starlingbank.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.starlingbank.TestApp;
import com.yolt.providers.starlingbank.common.model.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;

import static java.time.ZonedDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(httpsPort = 0, port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StarlingBankDataMapperV3Test {

    @Autowired
    @Qualifier("StarlingBankObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    private static final String EXTERNAL_ACCOUNT_ID = "b0b20c9d--42f1-a7d0-e70d4538e0d9";
    private static final String ACCOUNTS_JSON = "{\n" +
            "  \"accountUid\": \"b0b20c9d--42f1-a7d0-e70d4538e0d9\",\n" +
            "  \"defaultCategory\": \"a0de8f6a-faa9-40f8-a0b8-58205e722cd7\",\n" +
            "  \"currency\": \"GBP\",\n" +
            "  \"createdAt\": \"2017-05-08T12:34:21.000Z\"\n" +
            "}";
    private static final String IDENTIFIERS_JSON = "{\n" +
            "  \"accountIdentifier\": \"b0b20c9d--42f1-a7d0-e70d4538e0d9\",\n" +
            "  \"bankIdentifier\": \"a0de8f6a-faa9-40f8-a0b8-58205e722cd7 GBP\",\n" +
            "  \"iban\": \"GB50SRLG60837112345678\",\n" +
            "  \"bic\": \"SRLGGB2L\",\n" +
            "  \"accountIdentifier\": \"01234567\",\n" +
            "  \"bankIdentifier\": \"666666\"\n" +
            "}";
    private static final String HOLDER_JASON = "{" +
            "  \"accountHolderName\": \"Dave Bowman\"\n" +
            "}";
    private static final String BALANCE_JSON = "{\"clearedBalance\":{\"currency\":\"GBP\",\"minorUnits\":11223344},\"effectiveBalance\":{\"currency\":\"GBP\",\"minorUnits\":223344},\"pendingTransactions\":{\"currency\":\"GBP\",\"minorUnits\":11223344}}";
    private static final String TRANSACTIONS_JSON_ARRAY = "{\n" +
            "      \"feedItems\": [\n" +
            "        {\n" +
            "          \"feedItemUid\": \"11221122-1122-1122-1122-112211221122\",\n" +
            "          \"categoryUid\": \"ccddccdd-ccdd-ccdd-ccdd-ccddccddccdd\",\n" +
            "          \"amount\": {\n" +
            "            \"currency\": \"GBP\",\n" +
            "            \"minorUnits\": 11223344\n" +
            "          },\n" +
            "          \"sourceAmount\": {\n" +
            "            \"currency\": \"GBP\",\n" +
            "            \"minorUnits\": 11223344\n" +
            "          },\n" +
            "          \"direction\": \"IN\",\n" +
            "          \"updatedAt\": \"2017-07-05T18:27:02.335Z\",\n" +
            "          \"transactionTime\": \"2017-07-05T18:27:02.335Z\",\n" +
            "          \"settlementTime\": \"2017-07-05T18:27:02.335Z\",\n" +
            "          \"source\": \"MASTER_CARD\",\n" +
            "          \"sourceSubType\": \"CONTACTLESS\",\n" +
            "          \"status\": \"PENDING\",\n" +
            "          \"counterPartyType\": \"MERCHANT\",\n" +
            "          \"counterPartyUid\": \"68e16af4-c2c3-413b-bf93-1056b90097fa\",\n" +
            "          \"counterPartyName\": \"ExternalPayment\",\n" +
            "          \"counterPartySubEntityUid\": \"35d46207-d90e-483c-a40a-128cc4da4bee\",\n" +
            "          \"counterPartySubEntityName\": \"My Starling Business account\",\n" +
            "          \"counterPartySubEntityIdentifier\": \"608371\",\n" +
            "          \"counterPartySubEntitySubIdentifier\": \"12345678\",\n" +
            "          \"reference\": \"TESCO-STORES-6148      SOUTHAMPTON   GBR\",\n" +
            "          \"country\": \"GB\",\n" +
            "          \"spendingCategory\": \"BILLS_AND_SERVICES\",\n" +
            "          \"userNote\": \"Tax deductable, submit me to payroll\",\n" +
            "          \"roundUp\": {\n" +
            "            \"goalCategoryUid\": \"68e16af4-c2c3-413b-bf93-1056b90097fa\",\n" +
            "            \"amount\": {\n" +
            "              \"currency\": \"GBP\",\n" +
            "              \"minorUnits\": 11223344\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"feedItemUid\": \"11221122-1122-1122-1122-112211221123\",\n" +
            "          \"categoryUid\": \"ccddccdd-ccdd-ccdd-ccdd-ccddccddccdd\",\n" +
            "          \"amount\": {\n" +
            "            \"currency\": \"GBP\",\n" +
            "            \"minorUnits\": 11223366\n" +
            "          },\n" +
            "          \"sourceAmount\": {\n" +
            "            \"currency\": \"GBP\",\n" +
            "            \"minorUnits\": 11223366\n" +
            "          },\n" +
            "          \"direction\": \"OUT\",\n" +
            "          \"updatedAt\": \"2017-07-05T18:28:02.335Z\",\n" +
            "          \"transactionTime\": \"2017-07-05T18:28:02.335Z\",\n" +
            "          \"settlementTime\": \"2017-07-05T18:28:02.335Z\",\n" +
            "          \"source\": \"MASTER_CARD\",\n" +
            "          \"sourceSubType\": \"CONTACTLESS\",\n" +
            "          \"status\": \"SETTLED\",\n" +
            "          \"counterPartyType\": \"MERCHANT\",\n" +
            "          \"counterPartyUid\": \"68e16af4-c2c3-413b-bf93-1056b90097fa\",\n" +
            "          \"counterPartyName\": \"Tesco\",\n" +
            "          \"counterPartySubEntityUid\": \"35d46207-d90e-483c-a40a-128cc4da4bee\",\n" +
            "          \"counterPartySubEntityName\": \"My Starling Business account\",\n" +
            "          \"counterPartySubEntityIdentifier\": \"608371\",\n" +
            "          \"counterPartySubEntitySubIdentifier\": \"12345678\",\n" +
            "          \"reference\": \"TESCO-STORES-6148      SOUTHAMPTON   GBR\",\n" +
            "          \"country\": \"GB\",\n" +
            "          \"spendingCategory\": \"BILLS_AND_SERVICES\",\n" +
            "          \"userNote\": \"Tax deductable, submit me to payroll\",\n" +
            "          \"roundUp\": {\n" +
            "            \"goalCategoryUid\": \"68e16af4-c2c3-413b-bf93-1056b90097fa\",\n" +
            "            \"amount\": {\n" +
            "              \"currency\": \"GBP\",\n" +
            "              \"minorUnits\": -11223366\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }";

    @Test
    public void shouldReturnCorrectAccountForConvertAccountWithCorrectData() throws JsonProcessingException {
        // given
        BalancesResponseV2 balanceResponse = objectMapper.readValue(BALANCE_JSON, BalancesResponseV2.class);
        TransactionsResponseV2 transactionsResponse = objectMapper.readValue(TRANSACTIONS_JSON_ARRAY, TransactionsResponseV2.class);
        AccountV2 accountsResponse = objectMapper.readValue(ACCOUNTS_JSON, AccountV2.class);
        AccountIdentifiersV2 accountIdentifiersResponse = objectMapper.readValue(IDENTIFIERS_JSON, AccountIdentifiersV2.class);
        AccountHolderNameV2 accountHolderNameResponse = objectMapper.readValue(HOLDER_JASON, AccountHolderNameV2.class);

        // when
        ProviderAccountDTO providerAccountDTO = StarlingBankDataMapperV3.convertAccount(
                accountsResponse,
                accountIdentifiersResponse,
                balanceResponse,
                transactionsResponse,
                accountHolderNameResponse,
                clock);

        // then
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO.getLastRefreshed().minusNanos(1)).isBefore(now());
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("2233.44");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("112233.44");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountId()).isEqualTo(EXTERNAL_ACCOUNT_ID);
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("GB50SRLG60837112345678");
        assertThat(providerAccountDTO.getAccountNumber().getSecondaryIdentification()).isEqualTo("66666601234567");
        assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isEqualTo("Dave Bowman");

        assertThat(providerAccountDTO.getTransactions()).hasSize(2);

        ProviderTransactionDTO firstTransaction = providerAccountDTO.getTransactions().get(0);
        assertThat(firstTransaction.getDateTime()).isEqualTo("2017-07-05T19:27:02.335+01:00[Europe/London]");
        assertThat(firstTransaction.getAmount()).isEqualTo("112233.44");
        assertThat(firstTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(firstTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(firstTransaction.getDescription()).isEqualTo("ExternalPayment");
        assertThat(firstTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(firstTransaction.getMerchant()).isNull();

        // Validate after generic fields are set
        providerAccountDTO.validate();
        firstTransaction.validate();
    }

    @Test
    public void shouldReturnCorrectTransactionAmountForConvertAccountWhenNegativeAmount() throws JsonProcessingException {
        // given
        BalancesResponseV2 balanceResponse = objectMapper.readValue(BALANCE_JSON, BalancesResponseV2.class);
        TransactionsResponseV2 transactionsResponse = objectMapper.readValue(TRANSACTIONS_JSON_ARRAY, TransactionsResponseV2.class);
        AccountV2 accountsResponse = objectMapper.readValue(ACCOUNTS_JSON, AccountV2.class);
        AccountIdentifiersV2 accountIdentifiersResponse = objectMapper.readValue(IDENTIFIERS_JSON, AccountIdentifiersV2.class);
        AccountHolderNameV2 accountHolderNameResponse = objectMapper.readValue(HOLDER_JASON, AccountHolderNameV2.class);

        // when
        ProviderAccountDTO providerAccountDTO = StarlingBankDataMapperV3.convertAccount(
                accountsResponse,
                accountIdentifiersResponse,
                balanceResponse,
                transactionsResponse,
                accountHolderNameResponse,
                clock);

        // then
        ProviderTransactionDTO lastTransaction = providerAccountDTO.getTransactions().get(1);
        assertThat(lastTransaction.getAmount()).isEqualTo("112233.66");
        assertThat(lastTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
    }
}
