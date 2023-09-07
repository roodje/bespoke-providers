package com.yolt.providers.starlingbank.starlingbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.starlingbank.SampleAuthenticationMeans;
import com.yolt.providers.starlingbank.TestApp;
import com.yolt.providers.starlingbank.common.mapper.StarlingBankTokenMapper;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.constants.OAuth.CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/starlingbank/v2", files = "classpath:/starlingbank", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StarlingBankDataProviderV2IntegrationTest {

    private static final UUID USER_ID = UUID.fromString("07a540a2-7b91-11e9-8f9e-2a86e4085a59");
    private static final String ACCESS_TOKEN = "access-token";
    private static final String ACCESS_TOKEN_INVALID = "access-token-invalid";
    private static final String AUTHORIZATION_CODE = "auth-code";
    private static final String REDIRECT_URL = "http://yolt.com/callback/starlingbank";
    private static final String STATE = "state";

    @Autowired
    @Qualifier("StarlingBankDataProviderV8")
    private UrlDataProvider dataProvider;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(dataProvider);
    }

    @Autowired
    @Qualifier("StarlingBankObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    @Autowired
    private RestTemplateManager restTemplateManager;


    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private StarlingBankTokenMapper tokenMapper;

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        authenticationMeans = new SampleAuthenticationMeans().getAuthenticationMeans();
        tokenMapper = new StarlingBankTokenMapper(objectMapper, clock);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStepWithRedirectUrlForGetLoginInfoWithCorrectRequestData(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL).setState(STATE)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        RedirectStep response = (RedirectStep) dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        String loginUrl = response.getRedirectUrl();
        assertThat(loginUrl).contains("/authorize?response_type=code&client_id=api-key&redirect_uri=http://yolt.com/callback/starlingbank&state=state&scope=account:read%20account-holder-name:read%20account-holder-type:read%20account-identifier:read%20account-list:read%20balance:read%20savings-goal:read%20savings-goal-transfer:read%20transaction:read");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnNewAccessMeansForCreateNewAccessMeansWithCorrectRequestData(UrlDataProvider dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(AUTHORIZATION_CODE);

        // when
        AccessMeansOrStepDTO response = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        AccessMeansDTO accessMeans = response.getAccessMeans();
        assertThat(accessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeans.getAccessMeans()).isEqualTo("{\"accessToken\":\"access-token\",\"expiresIn\":300,\"refreshToken\":\"refresh-token\",\"tokenType\":\"Bearer\"}");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowMissingDataExceptionForCreateNewAccessMeansWhenAuthorizationCodeIsMissing(UrlDataProvider dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(null);

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isInstanceOf(MissingDataException.class)
                .hasMessage("Missing authorization code in redirect url query parameters");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnNewAccessMeansForRefreshAccessMeansWithCorrectRequestData(UrlDataProvider dataProvider) throws TokenInvalidException {
        // given
        AccessMeansDTO accessMeans = createAccessMeansWithRefreshToken("refresh-token-200");
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        AccessMeansDTO response = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getAccessMeans()).contains("eySpc6QnkRHQ4rCTP8E4I8alOzXtU63U9mNnNsPeBwUa50QbKIU");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowTokenInvalidExceptionForResponse400InvalidClientWhileRefreshingAccessMeans(UrlDataProvider dataProvider) throws TokenInvalidException {
        // given
        AccessMeansDTO accessMeans = createAccessMeansWithRefreshToken("refresh-token-400");
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage("Refresh token could not be verified, it could be invalid, expired or revoked: HTTP 400");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowTokenInvalidExceptionForRefreshAccessMeansFor401UnauthorizedToken(UrlDataProvider dataProvider) throws TokenInvalidException {
        // given
        AccessMeansDTO accessMeans = createAccessMeansWithRefreshToken("refresh-token-401");
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage("We are not authorized to call endpoint: HTTP 401");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnDataForFetchDataWithCorrectRequestData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        AccessMeansDTO accessMeans = createAccessMeansWithAccessToken(ACCESS_TOKEN);
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.parse("2019-06-03T12:23:25Z"))
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(response.getAccounts()).hasSize(2);

        ProviderAccountDTO providerAccountDTO = response.getAccounts().get(0);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("112233.44");
        assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isEqualTo("Dave Bowman");
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("GB63SRLG60837101234567");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getAccountNumber().getSecondaryIdentification()).isEqualTo("60837101234567");
        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSize(2);
        assertThat(providerAccountDTO.getExtendedAccount().getAccountReferences()).hasSize(2);
        assertThat(providerAccountDTO.getTransactions()).hasSize(2);

        ProviderTransactionDTO providerTransactionDTO = providerAccountDTO.getTransactions().get(0);
        assertThat(providerTransactionDTO.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(providerTransactionDTO.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(providerTransactionDTO.getAmount()).isEqualByComparingTo("112233.44");
        assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
        ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
        assertThat(extendedTransactionDTO.getTransactionAmount().getAmount()).isEqualByComparingTo("112233.44");
        assertThat(extendedTransactionDTO.getDebtorName()).isEqualTo("Endeavour Morse");
        assertThat(extendedTransactionDTO.getDebtorAccount().getValue()).isEqualTo("60837112345678");
        assertThat(extendedTransactionDTO.getProprietaryBankTransactionCode()).isEqualTo("FASTER_PAYMENTS_IN CONTACTLESS");

        providerTransactionDTO = providerAccountDTO.getTransactions().get(1);
        assertThat(providerTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(providerTransactionDTO.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(providerTransactionDTO.getAmount()).isEqualByComparingTo("112233.66");
        assertThat(providerTransactionDTO.getDateTime()).isEqualTo("2017-07-05T19:28:02.335+01:00[Europe/London]");
        assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
        extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
        assertThat(extendedTransactionDTO.getTransactionAmount().getAmount()).isEqualByComparingTo("-112233.66");
        assertThat(extendedTransactionDTO.getCreditorName()).isEqualTo("Tesco");
        assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo("2017-07-05T19:28:02.335+01:00[Europe/London]");
        assertThat(extendedTransactionDTO.getCreditorAccount().getValue()).isEqualTo("60837112345679");
        assertThat(extendedTransactionDTO.getProprietaryBankTransactionCode()).isEqualTo("MASTER_CARD CONTACTLESS");

        providerAccountDTO = response.getAccounts().get(1);
        assertThat(providerAccountDTO.getTransactions()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowTokenInvalidExceptionForFetchDataWhenUnauthorized(UrlDataProvider dataProvider) {
        // given
        AccessMeansDTO accessMeans = createAccessMeansWithAccessToken(ACCESS_TOKEN_INVALID);
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage("We are not authorized to call endpoint: HTTP 401");
    }

    private AccessMeansDTO createAccessMeansWithAccessToken(final String accessToken) {
        Token oAuthToken = new Token();
        oAuthToken.setUserId(USER_ID.toString());
        oAuthToken.setAccessToken(accessToken);
        oAuthToken.setTokenType("Bearer");
        oAuthToken.setRefreshToken("refresh-token");
        oAuthToken.setExpiresIn(300);
        return tokenMapper.mapToAccessMeansDTO(USER_ID, oAuthToken);
    }

    private AccessMeansDTO createAccessMeansWithRefreshToken(final String refreshToken) {
        Token oAuthToken = new Token();
        oAuthToken.setUserId(USER_ID.toString());
        oAuthToken.setAccessToken(ACCESS_TOKEN);
        oAuthToken.setTokenType("Bearer");
        oAuthToken.setRefreshToken(refreshToken);
        oAuthToken.setExpiresIn(300);
        return tokenMapper.mapToAccessMeansDTO(USER_ID, oAuthToken);
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(final String authorizationCode) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add(STATE, STATE);
        parameters.add(CODE, authorizationCode);

        String redirectUrlWithParameters = UriComponentsBuilder.fromUriString(REDIRECT_URL)
                .queryParams(parameters)
                .build()
                .toUriString();

        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrlWithParameters)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .build();
    }
}


