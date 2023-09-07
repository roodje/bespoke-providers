package com.yolt.providers.bancacomercialaromana.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancacomercialaromana.BcrSampleAuthenticationMeans;
import com.yolt.providers.bancacomercialaromana.TestApp;
import com.yolt.providers.bancacomercialaromana.common.model.Token;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.yolt.providers.bancacomercialaromana.common.auth.BcrGroupAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bcrgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/happyflow", port = 0, httpsPort = 0)
class BcrGroupDataProviderV1HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REDIRECT_URL = "https://yolt.com/callback/bcr";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("BcrGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private BcrGroupDataProvider subject;

    @BeforeAll
    static void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = BcrSampleAuthenticationMeans.getBcrSampleAuthenticationMeansForAis();
    }

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = subject.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).hasSize(7);
        assertThat(typedAuthMeans.get(CLIENT_ID_NAME)).isEqualTo(CLIENT_ID_STRING);
        assertThat(typedAuthMeans.get(CLIENT_SECRET_NAME)).isEqualTo(CLIENT_SECRET_STRING);
        assertThat(typedAuthMeans.get(CLIENT_TRANSPORT_KEY_ID_NAME)).isEqualTo(KEY_ID);
        assertThat(typedAuthMeans.get(CLIENT_SIGNING_KEY_ID_NAME)).isEqualTo(KEY_ID);
        assertThat(typedAuthMeans.get(CLIENT_TRANSPORT_CERTIFICATE_NAME)).isEqualTo(CLIENT_TRANSPORT_CERTIFICATE_PEM);
        assertThat(typedAuthMeans.get(CLIENT_SIGNING_CERTIFICATE_NAME)).isEqualTo(CLIENT_SIGNING_CERTIFICATE_PEM);
        assertThat(typedAuthMeans.get(WEB_API_KEY_NAME)).isEqualTo(API_KEY_STRING);
    }


    @Test
    void shouldGetLoginInfo() {
        // given
        String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL).setState(loginState)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        RedirectStep loginInfo = subject.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getRedirectUrl()).contains("/authorize?response_type=code&client_id=someClientId&redirect_uri=https://yolt.com/callback/bcr&scope=AISP%20PISP&state=" + loginState + "&access_type=offline");
    }

    @Test
    void shouldRefreshAccessMeans() throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(createAccessMeans(createToken(ACCESS_TOKEN)))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO response = subject.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getAccessMeans()).contains(ACCESS_TOKEN);
    }

    @Test
    void shouldCreateNewAccessMeans() {
        // given
        UrlCreateAccessMeansRequest createAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?state=state&code=auth-code")
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansOrStepDTO response = subject.createNewAccessMeans(createAccessMeansRequest);

        // then
        assertThat(response.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        assertThat(response.getAccessMeans().getAccessMeans()).contains(ACCESS_TOKEN);
    }

    @Test
    void shouldReturnAccountsAndTransactions() throws JsonProcessingException, TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeans(createToken(ACCESS_TOKEN)))
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        DataProviderResponse response = subject.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(3);

        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1).satisfies(validateProviderAccountDTO("1", "500", "95.5"));

        List<ProviderTransactionDTO> account1Transactions = account1.getTransactions();
        assertThat(account1Transactions).hasSize(3);
        assertThat(account1Transactions.get(0)).satisfies(validateProviderTransactionDTO("7509134266", "3.75", BOOKED, CREDIT, CurrencyCode.RON, "Transfer conturi proprii"));
        assertThat(account1Transactions.get(1)).satisfies(validateProviderTransactionDTO("8679655462", "0.07", BOOKED, DEBIT, CurrencyCode.EUR, "Virament automat din"));
        assertThat(account1Transactions.get(2)).satisfies(validateProviderTransactionDTO("7456512758", "3.75", BOOKED, CREDIT, CurrencyCode.RON, "Sepa Credit Transfer"));

        ProviderAccountDTO account2 = accounts.get(1);
        assertThat(account2).satisfies(validateProviderAccountDTO("2", "50", "112"));

        List<ProviderTransactionDTO> account2Transactions = account2.getTransactions();
        assertThat(account2Transactions).hasSize(2);
        assertThat(account2Transactions.get(0)).satisfies(validateProviderTransactionDTO("8679655462", "0.07", PENDING, DEBIT, CurrencyCode.EUR, "Virament automat din"));
        assertThat(account2Transactions.get(1)).satisfies(validateProviderTransactionDTO("7509134265", "5.75", BOOKED, CREDIT, CurrencyCode.RON, "Transfer conturi proprii"));

        ProviderAccountDTO account3 = accounts.get(2);
        assertThat(account3).satisfies(validateProviderAccountDTO("3", "51", "112"));

        List<ProviderTransactionDTO> account3Transactions = account3.getTransactions();
        assertThat(account3Transactions).isEmpty();
    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId, String availableBalance, String currentBalance) {
        return providerAccountDTO -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), within(32, ChronoUnit.SECONDS));
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(availableBalance);
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(currentBalance);
            assertThat(providerAccountDTO.getName()).isNotEmpty();
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.RON);

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(accountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(accountNumberDTO.getIdentification()).isNotEmpty();

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(CurrencyCode.RON);

            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(accountNumberDTO.getIdentification());

            List<BalanceDTO> balances = extendedAccountDTO.getBalances();
            assertThat(balances).isNotEmpty();
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String transactionId, String amount, TransactionStatus status, ProviderTransactionType type, CurrencyCode currencyCode
            , String remittanceInformationUnstructured) {
        return providerTransactionDTO -> {
            providerTransactionDTO.validate();
            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(transactionId);
            assertThat(providerTransactionDTO.getDateTime()).isNotNull();
            assertThat(providerTransactionDTO.getDateTime().getZone()).isEqualTo(ZoneId.of("Europe/Bucharest"));
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(amount);
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(providerTransactionDTO.getType()).isEqualTo(type);
            assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(extendedTransactionDTO.getBookingDate()).isNotNull();
            assertThat(extendedTransactionDTO.getBookingDate().getZone()).isEqualTo(ZoneId.of("Europe/Bucharest"));
            assertThat(extendedTransactionDTO.getValueDate()).isNotNull();
            assertThat(extendedTransactionDTO.getValueDate().getZone()).isEqualTo(ZoneId.of("Europe/Bucharest"));
            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo(remittanceInformationUnstructured);


            BalanceAmountDTO balanceAmountDTO = extendedTransactionDTO.getTransactionAmount();
            if (DEBIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount).negate());
            }
            if (CREDIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(amount);
            }
            assertThat(balanceAmountDTO.getCurrency()).isEqualTo(currencyCode);
        };
    }

    private AccessMeansDTO createAccessMeans(final Token oAuthToken) throws JsonProcessingException {
        String serializedOAuthToken = objectMapper.writeValueAsString(oAuthToken);
        return new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date());
    }

    private Token createToken(String accessToken) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(REFRESH_TOKEN);
        token.setExpiresIn(300L);
        return token;
    }
}
