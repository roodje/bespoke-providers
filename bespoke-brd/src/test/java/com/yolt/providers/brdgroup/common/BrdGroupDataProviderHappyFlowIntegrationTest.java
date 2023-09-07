package com.yolt.providers.brdgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.brdgroup.BrdGroupSampleAuthenticationMeans;
import com.yolt.providers.brdgroup.TestConfiguration;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("brd")
class BrdGroupDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Date UPDATED_DATE = parseDate("2020-01-02");
    private static final Date EXPIRATION_DATE = parseDate("2020-01-03");
    private static final String CONSENT_ID = "800000022";
    private static final String REDIRECT_URI = "https://yolt.com/callback";

    @Autowired
    @Qualifier("BrdGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("BrdDataProviderV1")
    private BrdGroupDataProvider dataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans = BrdGroupSampleAuthenticationMeans.get();

    @Test
    void shouldReturnFormStepWithLoginIDField() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setState("d79960be-3058-4d8e-9cd3-3753febd4661")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        FormStep formStep = dataProvider.getLoginInfo(request);

        // then
        assertThat(formStep.getEncryptionDetails()).isEqualTo(EncryptionDetails.noEncryption());
        assertThat(formStep.getTimeoutTime()).isCloseTo(Instant.now().plus(1L, HOURS), within(10, SECONDS));

        Form form = formStep.getForm();
        assertThat(form.getFormComponents()).hasSize(1);

        TextField loginIdTextField = (TextField) form.getFormComponents().get(0);
        assertThat(loginIdTextField.getId()).isEqualTo("LoginID");
        assertThat(loginIdTextField.getDisplayName()).isEqualTo("LoginID of the client, used in MyBRD applications");
        assertThat(loginIdTextField.getOptional()).isFalse();
    }

    @Test
    void shouldReturnNewAccessMeans() throws Exception {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setSigner(signer)
                .setState("7c3e98de-0239-4868-ada8-aefb5384ef0a")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setFilledInUserSiteFormValues(createFilledInUserSiteFormValues())
                .setUserId(USER_ID)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getStep()).isNull();

        AccessMeansDTO accessMeansDTO = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(request.getUserId());
        assertThat(accessMeansDTO.getUpdated()).isAfter(UPDATED_DATE);
        assertThat(accessMeansDTO.getExpireTime()).isAfter(EXPIRATION_DATE);

        BrdGroupAccessMeans accessMeans = objectMapper.readValue(accessMeansDTO.getAccessMeans(), BrdGroupAccessMeans.class);
        assertThat(accessMeans.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    void shouldSuccessfullyDeleteConsent() {
        UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest = new UrlOnUserSiteDeleteRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(accessMeansDTO())
                .build();

        assertThatCode(() -> dataProvider.onUserSiteDelete(urlOnUserSiteDeleteRequest))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowTokenInvalidExceptionDuringRefreshAccessMeans() {
        // given
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(accessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        assertThatThrownBy(() -> dataProvider.refreshAccessMeans(request))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldReturnAccountsAndTransactions() throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(accessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(2);

        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1).satisfies(validateProviderAccountDTO("1010000000195", "2020.55", "2020.55", CurrencyCode.RON));

        List<ProviderTransactionDTO> account1Transactions = account1.getTransactions();
        assertThat(account1Transactions).hasSize(5);
        assertThat(account1Transactions.get(0)).satisfies(validateProviderTransactionDTO("12323414235435",
                "5.67", BOOKED, CREDIT, "TEST 1",
                "2019-01-13T00:00+02:00[Europe/Bucharest]", "2019-01-12T00:00+02:00[Europe/Bucharest]", CurrencyCode.RON));
        assertThat(account1Transactions.get(2)).satisfies(validateProviderTransactionDTO(null, "35",
                PENDING, CREDIT, "COMERCIANT TEST",
                null, "2019-01-11T00:00+02:00[Europe/Bucharest]", CurrencyCode.RON));

        ProviderAccountDTO account2 = accounts.get(1);
        assertThat(account2).satisfies(validateProviderAccountDTO("1010000000285", "1111.44", "1111.44", CurrencyCode.EUR));

        List<ProviderTransactionDTO> account2Transactions = account2.getTransactions();
        assertThat(account2Transactions).hasSize(0);
    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId,
                                                                    String availableBalance,
                                                                    String currentBalance,
                                                                    CurrencyCode currencyCode) {
        return providerAccountDTO -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), within(32, SECONDS));
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal(availableBalance));
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal(currentBalance));
            assertThat(providerAccountDTO.getName()).isNotEmpty();
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(currencyCode);

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(accountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(accountNumberDTO.getIdentification()).isNotEmpty();

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(currencyCode);

            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(accountNumberDTO.getIdentification());

            List<BalanceDTO> balances = extendedAccountDTO.getBalances();
            assertThat(balances).isNotEmpty();
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String transactionId,
                                                                            String amount,
                                                                            TransactionStatus status,
                                                                            ProviderTransactionType type,
                                                                            String remittanceInformationUnstructured,
                                                                            String bookingDateTime,
                                                                            String valueDateTime,
                                                                            CurrencyCode currency) {
        return providerTransactionDTO -> {
            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(transactionId);
            if (bookingDateTime != null) {
                assertThat(providerTransactionDTO.getDateTime()).isEqualTo(bookingDateTime);
            }
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(providerTransactionDTO.getType()).isEqualTo(type);
            assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            if (bookingDateTime != null) {
                assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo(bookingDateTime);
            }
            assertThat(extendedTransactionDTO.getValueDate()).isEqualTo(valueDateTime);
            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo(remittanceInformationUnstructured);

            BalanceAmountDTO balanceAmountDTO = extendedTransactionDTO.getTransactionAmount();
            if (DEBIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount).negate());
            }
            if (CREDIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            }
            assertThat(balanceAmountDTO.getCurrency()).isEqualTo(currency);
        };
    }

    @SneakyThrows
    private AccessMeansDTO accessMeansDTO() {
        BrdGroupAccessMeans brdGroupAccessMeans = new BrdGroupAccessMeans(CONSENT_ID);
        return new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(brdGroupAccessMeans), UPDATED_DATE, EXPIRATION_DATE);
    }

    private FilledInUserSiteFormValues createFilledInUserSiteFormValues() {
        HashMap<String, String> valueMap = new HashMap<>(1);
        valueMap.put("LoginID", "customer-login");

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(valueMap);
        return filledInUserSiteFormValues;
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }
}