package com.yolt.providers.nutmeggroup.nutmeg;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.nutmeggroup.SampleTypedAuthenticationMeansV2;
import com.yolt.providers.nutmeggroup.TestApp;
import com.yolt.providers.nutmeggroup.TestRestTemplateManager;
import com.yolt.providers.nutmeggroup.common.OAuthConstants;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings", httpsPort = 0, port = 0)
public class NutmegDataProviderV2IntegrationTest {

    // getLoginInfo() expectations
    private static final int EXPECTED_CODE_VERIFIER_LENGTH = 42;
    private static final String EXPECTED_CODE = "code";
    private static final String EXPECTED_CODE_CHALLENGE_METHOD = "S256";
    private static final String EXPECTED_CLIENT_ID = "clientId";
    private static final String EXPECTED_SCOPE = "read:balance%20offline_access";
    private static final String EXPECTED_REDIRECT_URI = "https://www.yolt.com/callback";
    private static final String EXPECTED_AUDIENCE = "https://api.nutmeg.com";
    private static final String EXPECTED_STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";

    // createNewAccessMeans() expectations
    private static final String EXPECTED_ACCESS_TOKEN = "{\"access_token\":\"ACCESS_TOKEN\",\"refresh_token\":\"REFRESH_TOKEN\",\"token_type\":\"Bearer\",\"expires_in\":10800}";

    // refreshAccessMeans() expectations
    private static final String EXPECTED_REFRESH_TOKEN = "{\"access_token\":\"ACCESS_TOKEN2\",\"refresh_token\":\"REFRESH_TOKEN\",\"token_type\":\"Bearer\",\"expires_in\":10700}";

    // fetchData() expectations
    private static final int EXPECTED_ACCOUNTS_SIZE = 2;
    private static final String EXPECTED_ACCOUNT_ID = "31728d06-474f-45fb-a5fd-e73f0333face";
    private static final BigDecimal EXPECTED_AVAILABLE_AND_CURRENT_BALANCE = new BigDecimal("1006.5");
    private static final CurrencyCode EXPECTED_CURRENCY = CurrencyCode.GBP;
    private static final String EXPECTED_NAME = "pot name";
    private static final AccountType EXPECTED_YOLT_ACCOUNT_TYPE = AccountType.PENSION;
    private static final int EXPECTED_AMOUNT_OF_TRANSACTIONS = 0;

    private static final String EXPECTED_RESOURCE_ID = "31728d06-474f-45fb-a5fd-e73f0333face";
    private static final ExternalCashAccountType EXPECTED_CASH_ACCOUNT_TYPE = ExternalCashAccountType.SAVINGS;
    private static final int EXPECTED_BALANCES_SIZE = 1;

    private static final BalanceType EXPECTED_BALANCE_TYPE = BalanceType.INTERIM_AVAILABLE;

    // Auxiliary constants
    private static final String REDIRECT_URI = "https://www.yolt.com/callback";
    private static final String REDIRECT_URI_WITH_PARAMS = "http://yolt.com?code=authorization_code&state=uniquestring";
    private static final String STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
    private static final String CODE_VERIFIER = "6965646a-e758-4478-8b22-312a96472b856965646a-e758-4478-8b22-312a96472b85";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final Date DATE = new Date();

    @Autowired
    private NutmegDataProviderV2 provider;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private UrlCreateAccessMeansRequest urlCreateAccessMeans;

    @BeforeEach
    public void setup() {
        authenticationMeans = new SampleTypedAuthenticationMeansV2().getAuthenticationMeans();
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URI_WITH_PARAMS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setProviderState(CODE_VERIFIER)
                .build();
    }

    @Test
    public void shouldReturnCorrectLoginPageUrl() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setState(STATE)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = provider.getLoginInfo(request);

        // then
        String codeVerifier = redirectStep.getProviderState();
        Map<String, String> redirectUrl = UriComponentsBuilder
                .fromUriString(redirectStep.getRedirectUrl())
                .build(false)
                .getQueryParams()
                .toSingleValueMap();

        assertThat(codeVerifier.length()).isGreaterThanOrEqualTo(EXPECTED_CODE_VERIFIER_LENGTH);
        assertThat(redirectUrl.get(OAuthConstants.Params.RESPONSE_TYPE)).isEqualTo(EXPECTED_CODE);
        assertThat(redirectUrl.get(OAuthConstants.Params.CODE_CHALLENGE_METHOD)).isEqualTo(EXPECTED_CODE_CHALLENGE_METHOD);
        assertThat(redirectUrl.get(OAuthConstants.Params.CLIENT_ID)).isEqualTo(EXPECTED_CLIENT_ID);
        assertThat(redirectUrl.get(OAuthConstants.Params.SCOPE)).isEqualTo(EXPECTED_SCOPE);
        assertThat(redirectUrl.get(OAuthConstants.Params.REDIRECT_URI)).isEqualTo(EXPECTED_REDIRECT_URI);
        assertThat(redirectUrl.get(OAuthConstants.Params.AUDIENCE)).isEqualTo(EXPECTED_AUDIENCE);
        assertThat(redirectUrl.get(OAuthConstants.Params.STATE)).isEqualTo(EXPECTED_STATE);
    }

    @Test
    public void shouldCreateNewAccessMeans() {
        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = provider.createNewAccessMeans(urlCreateAccessMeans);
        String accessToken = accessMeansOrStepDTO.getAccessMeans().getAccessMeans();

        // then
        assertThat(accessToken).isEqualTo(EXPECTED_ACCESS_TOKEN);
    }

    @Test
    public void shouldRefreshAccessMeans() throws Exception {
        // given
        AccessMeansOrStepDTO accessMeansOrStepDTO = provider.createNewAccessMeans(urlCreateAccessMeans);
        String accessToken = accessMeansOrStepDTO.getAccessMeans().getAccessMeans();

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, accessToken, DATE, DATE);
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = provider.refreshAccessMeans(urlRefreshAccessMeans);
        String refreshToken = accessMeansDTO.getAccessMeans();

        // then
        assertThat(refreshToken).isEqualTo(EXPECTED_REFRESH_TOKEN);
    }

    @Test
    public void shouldSuccessfullyFetchData() throws ProviderFetchDataException, TokenInvalidException {
        // given
        AccessMeansOrStepDTO accessMeansOrStepDTO = provider.createNewAccessMeans(urlCreateAccessMeans);
        AccessMeansDTO accessMeans = accessMeansOrStepDTO.getAccessMeans();

        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(null)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();

        // amount of accounts verification
        assertThat(accounts).hasSize(EXPECTED_ACCOUNTS_SIZE);

        ProviderAccountDTO providerAccountDTO = accounts.get(0);
        ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();

        // providerAccountDTO validation
        providerAccountDTO.validate();

        // ProviderAccountDTO verification
        assertThat(providerAccountDTO.getAccountId()).isEqualTo(EXPECTED_ACCOUNT_ID);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(EXPECTED_AVAILABLE_AND_CURRENT_BALANCE);
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(EXPECTED_CURRENCY);
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(EXPECTED_AVAILABLE_AND_CURRENT_BALANCE);
        assertThat(providerAccountDTO.getName()).isEqualTo(EXPECTED_NAME);
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(EXPECTED_YOLT_ACCOUNT_TYPE);
        assertThat(providerAccountDTO.getTransactions()).hasSize(EXPECTED_AMOUNT_OF_TRANSACTIONS);
        assertThat(extendedAccountDTO).isNotNull();

        List<BalanceDTO> balances = extendedAccountDTO.getBalances();

        // ExtendedAccountDTO verification
        assertThat(extendedAccountDTO.getResourceId()).isEqualTo(EXPECTED_RESOURCE_ID);
        assertThat(extendedAccountDTO.getCurrency()).isEqualTo(EXPECTED_CURRENCY);
        assertThat(extendedAccountDTO.getName()).isEqualTo(EXPECTED_NAME);
        assertThat(extendedAccountDTO.getCashAccountType()).isEqualTo(EXPECTED_CASH_ACCOUNT_TYPE);
        assertThat(balances).isNotNull();
        assertThat(balances).hasSize(EXPECTED_BALANCES_SIZE);

        BalanceDTO balance = balances.get(0);
        BalanceAmountDTO balanceAmount = balance.getBalanceAmount();

        // BalanceDTO verification
        assertThat(balanceAmount).isNotNull();
        assertThat(balanceAmount.getAmount()).isEqualTo(EXPECTED_AVAILABLE_AND_CURRENT_BALANCE);
        assertThat(balanceAmount.getCurrency()).isEqualTo(EXPECTED_CURRENCY);
        assertThat(balance.getBalanceType()).isEqualTo(EXPECTED_BALANCE_TYPE);
    }
}
