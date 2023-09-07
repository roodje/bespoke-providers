package com.yolt.providers.bancatransilvania.common;

import com.yolt.providers.bancatransilvania.common.domain.BancaTransilvaniaGroupProviderState;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
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
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeansProducerV1.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * This test contains all happy flows occurring in Banca Transilvania group providers.
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * - fetching accounts and transactions
 * - handling too many requests
 * <p>
 */
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow/", httpsPort = 0, port = 0)
class BancaTransilvaniaGroupDataProviderHappyFlowIntegrationTest extends BancaTransilvaniaGroupDataProviderTestBaseSetup {

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).hasSize(10);
        assertThat(typedAuthMeans.get(CLIENT_NAME)).isEqualTo(CLIENT_NAME_TYPE);
        assertThat(typedAuthMeans.get(CLIENT_COMPANY_NAME)).isEqualTo(CLIENT_COMPANY_NAME_TYPE);
        assertThat(typedAuthMeans.get(CLIENT_WEBSITE_URI_NAME)).isEqualTo(CLIENT_WEBSITE_URI_TYPE);
        assertThat(typedAuthMeans.get(CLIENT_CONTACT_NAME)).isEqualTo(CLIENT_CONTACT_NAME_TYPE);
        assertThat(typedAuthMeans.get(CLIENT_EMAIL_NAME)).isEqualTo(CLIENT_EMAIL);
        assertThat(typedAuthMeans.get(CLIENT_PHONE_NAME)).isEqualTo(CLIENT_PHONE_TYPE);
        assertThat(typedAuthMeans.get(CLIENT_ID_NAME)).isEqualTo(CLIENT_ID_STRING);
        assertThat(typedAuthMeans.get(CLIENT_SECRET_NAME)).isEqualTo(CLIENT_SECRET_STRING);
        assertThat(typedAuthMeans.get(TRANSPORT_KEY_ID_NAME)).isEqualTo(KEY_ID);
        assertThat(typedAuthMeans.get(TRANSPORT_CERTIFICATE_NAME)).isEqualTo(CERTIFICATE_PEM);
    }

    @Test
    void shouldReturnTypedAuthenticationMeansForRegistration() {
        // when
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = dataProvider.getAutoConfiguredMeans();

        // then
        assertThat(autoConfiguredMeans).hasSize(2);
        assertThat(autoConfiguredMeans.get(CLIENT_ID_NAME)).isEqualTo(CLIENT_ID_STRING);
        assertThat(autoConfiguredMeans.get(CLIENT_SECRET_NAME)).isEqualTo(CLIENT_SECRET_STRING);
    }

    @Test
    void shouldReturnDefaultAndRegisteredAuthenticationMeans() {
        // given
        Map<String, BasicAuthenticationMean> unregisteredAuthenticationMeans = new HashMap<>(authenticationMeans);
        unregisteredAuthenticationMeans.remove(CLIENT_ID_NAME);
        unregisteredAuthenticationMeans.remove(CLIENT_SECRET_NAME);

        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(unregisteredAuthenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .setRedirectUrls(List.of("https://client-redirect.yts.yolt.io"))
                .build();

        // when
        Map<String, BasicAuthenticationMean> authMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(authMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isEqualTo(pemCertificate);
        assertThat(authMeans.get(TRANSPORT_KEY_ID_NAME).getValue()).isEqualTo("2be4d475-f240-42c7-a22c-882566ac0f95");
        assertThat(authMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("registered-client-id");
        assertThat(authMeans.get(CLIENT_SECRET_NAME).getValue()).isEqualTo("registered-client-secret");
    }

    @Test
    void shouldReturnRedirectStepWithConsentUrl() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setState("d79960be-3058-4d8e-9cd3-3753febd4661")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .build();

        // when
        RedirectStep redirectStep = dataProvider.getLoginInfo(request);

        // then
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains("/prd/sps/oauth/oauth20/authorize");

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(loginUrl).build()
                .getQueryParams()
                .toSingleValueMap();

        assertThat(queryParams.get("response_type")).isEqualTo("code");
        assertThat(queryParams.get("client_id")).isEqualTo(CLIENT_ID);
        assertThat(queryParams.get("scope")).isEqualTo("AIS:" + CONSENT_ID);
        assertThat(queryParams.get("state")).isEqualTo("d79960be-3058-4d8e-9cd3-3753febd4661");
        assertThat(queryParams.get("redirect_uri")).isEqualTo(REDIRECT_URI);
        assertThat(queryParams.get("code_challenge")).isNotEmpty();
        assertThat(queryParams.get("code_challenge_method")).isEqualTo("S256");
    }

    @Test
    void shouldReturnNewAccessMeans() throws TokenInvalidException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setSigner(signer)
                .setState("29dbba15-1e67-4ac0-ab0f-2487dc0c960b")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(providerStateMapper.toJson(createProviderState()))
                .setRedirectUrlPostedBackFromSite("https://yolt.com/callback?state=29dbba15-1e67-4ac0-ab0f-2487dc0c960b&code=authorization-code")
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

        BancaTransilvaniaGroupProviderState providerState = providerStateMapper.fromJson(accessMeansDTO.getAccessMeans());
        assertThat(providerState.getCodeVerifier()).isEqualTo(codeExchange.getCodeVerifier());
        assertThat(providerState.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(providerState.getAccessToken()).isEqualTo("access-token");
        assertThat(providerState.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void shouldReturnRefreshedAccessMeans() throws TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansDTO.getUpdated()).isAfter(UPDATED_DATE);
        assertThat(accessMeansDTO.getExpireTime()).isAfter(EXPIRATION_DATE);

        BancaTransilvaniaGroupProviderState providerState = providerStateMapper.fromJson(accessMeansDTO.getAccessMeans());
        assertThat(providerState.getCodeVerifier()).isEqualTo(codeExchange.getCodeVerifier());
        assertThat(providerState.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(providerState.getAccessToken()).isEqualTo("new-access-token");
        assertThat(providerState.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void shouldReturnAccountsAndTransactions() throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest request = buildGenericFetchDataRequest();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(2);

        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1).satisfies(validateProviderAccountDTO("1", BigDecimal.valueOf(510.10), BigDecimal.valueOf(510.10)));

        List<ProviderTransactionDTO> account1Transactions = account1.getTransactions();
        assertThat(account1Transactions).hasSize(4);
        assertThat(account1Transactions.get(0)).satisfies(validateProviderTransactionDTO("313ATCW1823600GV", "0.10", BOOKED, CREDIT, null));
        assertThat(account1Transactions.get(1)).satisfies(validateProviderTransactionDTO("766ATCW18223009Z", "200.10", BOOKED, DEBIT, "Tranzactie comerciant - Tranz: Nr card 9999XXXXXX9999. Data_Ora: 23-02-2019 17:10:30"));
        assertThat(account1Transactions.get(3)).satisfies(validateProviderTransactionDTO("028ZTRF182730XRR", "400.20", BOOKED, CREDIT, "Tranzactie comerciant - Tranz: Nr card 9999XXXXXX9999. Data_Ora: 23-02-2019 17:10:30"));

        ProviderAccountDTO account2 = accounts.get(1);
        assertThat(account2).satisfies(validateProviderAccountDTO("2", BigDecimal.valueOf(50.10), BigDecimal.valueOf(50.10)));

        List<ProviderTransactionDTO> account2Transactions = account2.getTransactions();
        assertThat(account2Transactions).isEmpty();
    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId, BigDecimal availableBalance, BigDecimal currentBalance) {
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
            assertThat(balances).hasSize(1);
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String transactionId,
                                                                            String amount,
                                                                            TransactionStatus status,
                                                                            ProviderTransactionType type,
                                                                            String remittanceInformationUnstructured) {
        return providerTransactionDTO -> {
            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(transactionId);
            assertThat(providerTransactionDTO.getDateTime()).isNotNull();
            assertThat(providerTransactionDTO.getDateTime().getZone()).isEqualTo(ZoneId.of("Europe/Bucharest"));
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount));
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
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            }
            assertThat(balanceAmountDTO.getCurrency()).isEqualTo(CurrencyCode.RON);
        };
    }
}
