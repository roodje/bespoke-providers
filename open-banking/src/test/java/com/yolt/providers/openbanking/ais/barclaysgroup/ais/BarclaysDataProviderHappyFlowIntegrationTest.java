package com.yolt.providers.openbanking.ais.barclaysgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysApp;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.dto.BarclaysLoginFormDTO;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.utils.JwtHelper;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.yolt.providers.common.constants.OAuth.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test contains all happy flows occurring in Barclays provider.
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/barclaysgroup/ais-3.1/v3/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("barclays")
class BarclaysDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final String CLAIMS_MARKER = "request=";
    private static final String CONSENT_ID = "BARCLAYS-A-10000004190382";
    private static String SERIALIZED_ACCESS_MEANS;

    private static final Signer SIGNER = new SignerMock();

    private RestTemplateManagerMock restTemplateManagerMock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private String requestTraceId = "d0a9b85f-9715-4d16-a33d-4323ceab5254";

    @Autowired
    @Qualifier("BarclaysDataProviderV16")
    private GenericBaseDataProviderV2 barclaysDataProviderV16;

    @Autowired
    @Qualifier("BarclaysObjectMapperV2")
    private ObjectMapper objectMapper;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(barclaysDataProviderV16);
    }

    private Stream<Arguments> getProvidersWithAudienceRegexpAndExpirationTime() {
        return Stream.of(Arguments.of(barclaysDataProviderV16, "https://personalBarclays.com", 6)
        );
    }

    private Stream<Arguments> getProvidersWithAccessMeansStateType() {
        return Stream.of(Arguments.of(barclaysDataProviderV16, new TypeReference<AccessMeansState>() {
        }));
    }

    @BeforeAll
    public void setup() throws JsonProcessingException {
        AccessMeansState<AccessMeans> accessMeansState = new AccessMeansState<>(new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL),
                List.of("ReadParty"));
        SERIALIZED_ACCESS_MEANS = objectMapper.writeValueAsString(accessMeansState);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new BarclaysSampleTypedAuthenticationMeans().getAuthenticationMean();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnFormStepWithProperComponentsForGetLoginInfoWithCorrectRequestData(UrlDataProvider subject) {
        // given
        String loginState = UUID.randomUUID().toString();

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        FormStep formStep = (FormStep) subject.getLoginInfo(urlGetLogin);

        // then
        List<SelectOptionValue> selectOptionValues = ((SelectField) formStep.getForm().getFormComponents().get(0)).getSelectOptionValues();
        assertThat(selectOptionValues).containsExactlyInAnyOrderElementsOf(List.of(
                new SelectOptionValue("PERSONAL", "Personal Customer"),
                new SelectOptionValue("BARCLAYS_CARD", "Barclays Card"),
                new SelectOptionValue("BUSINESS", "Business Banking customer")

        ));
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithAudienceRegexpAndExpirationTime")
    void shouldReturnRedirectStepIfTriggeredAfterFormStep(UrlDataProvider subject,
                                                          String audienceRegexp,
                                                          long expirationTimeInMinutes) throws JsonProcessingException {
        // given
        String clientId = "someClientId";
        String stateId = UUID.randomUUID().toString();
        String redirectUrl = "http://yolt.com/identifier";
        BarclaysLoginFormDTO barclaysLoginForm = new BarclaysLoginFormDTO(new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()), redirectUrl);
        String expectedUrlRegex = "^https://personalBarclays.com/as/authorization.oauth2\\?response_type=code\\+id_token&client_id=" + clientId + "&state=" + stateId + "&scope=openid\\+accounts&nonce=" + stateId + "&redirect_uri=http%3A%2F%2Fyolt\\.com%2Fidentifier&request=.*";
        String providerState = objectMapper.writeValueAsString(barclaysLoginForm);
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("AccountType", "PERSONAL");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setProviderState(providerState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManagerMock)
                .setState(stateId)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) subject.createNewAccessMeans(urlCreateAccessMeansRequest).getStep();

        // then
        Map<String, Object> claimsMap = JwtHelper.parseJwtClaims(redirectStep.getRedirectUrl()
                .substring(redirectStep.getRedirectUrl().indexOf(CLAIMS_MARKER) + CLAIMS_MARKER.length())).getClaimsMap();
        assertThat(claimsMap.get("aud").toString()).matches(audienceRegexp);
        NumericDate expectedExpirationDateTime = NumericDate.fromSeconds(ZonedDateTime.now(ZoneId.of("Europe/London")).plusMinutes(expirationTimeInMinutes).toEpochSecond());
        NumericDate actualExpirationDateTime = NumericDate.fromSeconds(Long.parseLong(claimsMap.get("exp").toString()));
        assertThat(actualExpirationDateTime.isBefore(expectedExpirationDateTime)).isTrue();
        claimsMap.remove("aud");
        claimsMap.remove("exp");
        assertThat(claimsMap).usingRecursiveComparison().isEqualTo(getExpectedClaimsMap(clientId, redirectUrl, stateId, CONSENT_ID));
        assertThat(redirectStep.getRedirectUrl()).matches(expectedUrlRegex);
        assertThat(redirectStep.getExternalConsentId()).isEqualTo(CONSENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldSuccessfullyFetchData(UrlDataProvider subject) throws Exception {
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts())
                .extracting(ProviderAccountDTO::getAccountId)
                .doesNotContain("10000000000001412345") // Travel Wallet account ID
                .hasSize(4);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        //Verify Current Account
        ProviderAccountDTO currentAccount = dataProviderResponse.getAccounts().get(0);
        validateCurrentAccount(currentAccount);

        //Verify second current Account
        ProviderAccountDTO secondCurrentAccount = dataProviderResponse.getAccounts().get(1);
        validateSecondCurrentAccount(secondCurrentAccount);

        //Verify Savings Account
        ProviderAccountDTO savingsAccount = dataProviderResponse.getAccounts().get(2);
        validateSavingsAccount(savingsAccount);

        //Verify Credit Card
        ProviderAccountDTO creditAccount = dataProviderResponse.getAccounts().get(3);
        validateCreditCardAccount(creditAccount);
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithAccessMeansStateType")
    void shouldReturnRefreshedAccessMeans(UrlDataProvider subject, TypeReference type) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO retrievedAccessMeans = subject.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        AccessMeans deserializedOAuthToken = extractAccessMeansFromSerializedJson(retrievedAccessMeans.getAccessMeans(), type);
        assertThat(retrievedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("eyJhbGciOiJkxVIiLCJlbmMiOiJBMjU2R0NNIiwiY3R5IjoiSldUIiwicGkuYXRtIjoiCSJ9..vG_C0m_tK_03UnpO.6Qm2A4xusMJogqiZS2dkTih9CtHiZaqXjZG9Ph9C1zVSuaiGa-Mw9w50A-TxBsnYF6vJdo-7khJvZAzfJnhXprSI89WwY9wqL91uqcwv8eOqE3OWuJ1VDGYxPnMebIPgFzpojNq5lIk5X6DbQWdlv2eHqFY9t7wkOg1szQkIsHUmVTPez_RaIXF-bNNbeITHoycj_Ii4KvHuv6yUpY7Lzo5pJ3AGzc6-Yv2uE9rv0PMhP-nnaXusHD1cb9anSLQ4RvYTLXDp3TezsUhEu-nw7CvmvLI9pYC8qiBNFCkgkzt2sXng_rFW4Ua2OTt6NFC9iWFtiF165Wm611Wwbd46dtT59vL4RCPd0g_iemqSNuB8dtuKrb0CcJR8lb7HNwBbXq63lH8YLTWjKg49rvok9wvj-NvNXEt_vmS5QizsrDAMpHTyBIaZPZSJHdXbpJICKKcFcJlvthggPzfBHfsYBdMrS1YEtvURP8Wr5TiuO0KsJHGWmyIzRy4vwkaiXBUnvx_qGj0Kuvwn6VNxJQHA7wGoy7tRibxhMS73isX3C7gty-NZPi8-GlmtSd1dqQwXZadbZS8emXbt9a6s-1OgJt7ebS9O95Lt-QY2Jzv1shqbl9u9FqlWQyCNq_Ai9ITneAZpMbVXdqXoK8ppxHgqoHyuwEYYY_iSTgriVTy4oQsaZI6_0JTOmK9MhBgpON3_XWx0ydHqCB-8oy7fU_2QjmPXMYfHBkar4uKBt8aYWUsfKuPINXVpjc5nqJW-LwPBumMinCR8PsEqJO63RoVwVdBGMhIFvPaeFxtnGk-rQatL2CdGryVcY4OGBZlDN0AQrRT-6geFY2E33Tx6ewYBSLKQYbc1vIiQDWXVqSvO5REB_aEurAm-uJarcyTwZRXUnRKMaC-SeJj7nNRVpj-eblakq-PVOGSUj3qUmALb5IOTr4OX7pINOiQS1BTBYl77lxqH4Lqm1rGlLGJLKBsXUKtwicGg_ED2X3FdGAUa2hP44mGufwmFKxnR0WHvWIVjr-Sla1K9h9UszgJpIXBtoYzUf_WgfIdtuOB8P8u85J-InqzPe85AGQyDQvZ5PofIkgHcVzcF1auypsnJIfzRwNP_esappXHZmjKp6b3Ewkz3R9BfkY9le4KK4KYAJck4kONXiixuqaLvkqjSnkcjbPlW9AWfTO4HYlg9vu0by_7L9YtdDQ6k3133npvyHDnEFAR0GFpiSWFqyUPP2OTxxPai4g1R2YkN5a6VRmGTUIPYIU7SAdW7icjpRu-LEhWygLU71tvC9_TRrFp79UBuwpYcDQMLRfw0uW0RT1XPBml_aEZpyuqGkwgX1C_gA05D7GuF-WJjciicMbBLCY9MdHN4SCukMUTY7zsjryPcFITRvmqU6H4FutiJwpi0eOMM-gIoVlUfh6V8Lt__UOl5n-G921WXI7TWvrA_9FDmY-d36YpjAdREnr33D4acD0iVJPiKEeHrK13VLy_PWYPKByrXCCEKTMdWcMebQfkSTJL1sUoiIYVEt5Y7sTgHYnInzX1ZmJDi91Dt9Ri4P4wJMt3hJ-LvgCUQ3F1pRRzZcwfA0QPINCbBRz4mYTHgz519-fM0a5zzksDj_mmF2KQPPY5sHundFTAxbvyIojc4tZCAooLj2CQamfAujLF7fAzKO_1Kp83Rp4BYM3_Rtv3-2jdhQx1y0ZF0LWLxhTWQWA.Uxqv97K6w-DAe_WVTwOQeA");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("mvHiKUZ1wIng1sxQPh587uX3Sl96E5NfXYus7drViL");
    }

    private AccessMeans extractAccessMeansFromSerializedJson(String json, TypeReference type) throws JsonProcessingException {
        var accessMeansState = objectMapper.readValue(json, type);
        return accessMeansState instanceof AccessMeansState ?
                ((AccessMeansState<?>) accessMeansState).getAccessMeans() :
                (AccessMeans) accessMeansState;
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldCreateNewAccessMeans(UrlDataProvider subject) throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";

        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545#code=" + authorizationCode + "&state=secretState";
        String encodedUrl = URLEncoder.encode("https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545", "UTF-8");

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(userId)
                .setProviderState("""
                        {"permissions":["ReadParty",\
                        "ReadAccountsDetail",\
                        "ReadBalances",\
                        "ReadDirectDebits",\
                        "ReadProducts",\
                        "ReadStandingOrdersDetail",\
                        "ReadTransactionsCredits",\
                        "ReadTransactionsDebits",\
                        "ReadTransactionsDetail"]}\
                        """)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansOrStepDTO newAccessMeans = subject.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(newAccessMeans).isNotNull();
    }

    @Test
    void shouldDeserializeTokenWithUnknownFields() throws IOException {
        // given
        String expectedAccessToken = "at12345";
        String expectedRefreshToken = "rt2345";
        String expectedExpireTime = "2018-01-11T12:13:14.123Z";
        String input = String.format(
                "{\"unknownField\": null, \"unknownField2\": null, \"accessToken\": \"%s\", \"refreshToken\": \"%s\", \"expireTime\": \"%s\"}",
                expectedAccessToken,
                expectedRefreshToken,
                expectedExpireTime);

        // when
        AccessMeans output = objectMapper.readValue(input, AccessMeans.class);

        // then
        assertThat(output.getAccessToken()).isEqualTo(expectedAccessToken);
        assertThat(output.getRefreshToken()).isEqualTo(expectedRefreshToken);
        assertThat(output.getExpireTime()).isEqualTo(Date.from(Instant.parse(expectedExpireTime)));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldDeleteUserSite(UrlDataProvider subject) {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("363ca7c1-9d03-4876-8766-ddefc9fd2d76")
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> subject.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    private void validateCurrentAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("10000000000001449160");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("10.12");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("10.12");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("10272100577464");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isEqualTo("MR User Name");
        assertThat(providerAccountDTO.getName()).isEqualTo("Barclays Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSameElementsAs(getExtendedBalancesForCurrentAccount());

        // Verify Standing Order
        assertThat(providerAccountDTO.getStandingOrders()).hasSize(2);
        StandingOrderDTO standingOrderDTO = providerAccountDTO.getStandingOrders().get(0);
        assertThat(standingOrderDTO.getDescription()).isEmpty();
        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ofMonths(1));
        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo("530.00");
        assertThat(standingOrderDTO.getCounterParty().getIdentification()).isEqualTo("20269601871019");

        // Verify Direct Debit
        assertThat(providerAccountDTO.getDirectDebits()).hasSize(2);
        DirectDebitDTO directDebitDTO = providerAccountDTO.getDirectDebits().get(0);
        assertThat(directDebitDTO.getDescription()).isEqualTo("PAYMENT");
        assertThat(directDebitDTO.isDirectDebitStatus()).isTrue();
        assertThat(directDebitDTO.getPreviousPaymentAmount()).isEqualTo("39.98");

        validateCurrentTransactions(providerAccountDTO.getTransactions());
    }

    private void validateSecondCurrentAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("20000000000001449160");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("9.12");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("11.12");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("10272100577465");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isEqualTo("MR User Name2");
        assertThat(providerAccountDTO.getName()).isEqualTo("Barclays Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSameElementsAs(getExtendedBalancesForSecondCurrentAccount());

        // Verify Standing Orders
        assertThat(providerAccountDTO.getStandingOrders()).isEmpty();

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits()).isEmpty();

        // Verify transactions
        assertThat(providerAccountDTO.getTransactions()).isEmpty();
    }

    private void validateCreditCardAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("10000000000001801829");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("1200.00");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("-10.20");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber()).isNull();
        assertThat(providerAccountDTO.getName()).isEqualTo("MR ROBIN HOOD");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(providerAccountDTO.getAccountMaskedIdentification()).isEqualTo("************0002");
        assertThat(providerAccountDTO.getCreditCardData().getAvailableCreditAmount()).isEqualTo("1200.00");

        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSameElementsAs(getExtendedBalancesForCreditCard());

        validateCreditCardTransactions(providerAccountDTO.getTransactions());
    }

    private void validateSavingsAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("10000000000000947108");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("0.58");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("0.58");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isNull();
        assertThat(providerAccountDTO.getName()).isEqualTo("Barclays Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);

        // Verify Standing Order
        assertThat(providerAccountDTO.getStandingOrders()).isEmpty();

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits()).isEmpty();

        assertThat(providerAccountDTO.getTransactions()).isEmpty();
    }

    private void validateCurrentTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(5);

        ProviderTransactionDTO pendingTransaction = transactions.get(0);
        assertThat(pendingTransaction.getAmount()).isEqualTo("0.09");
        assertThat(pendingTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(pendingTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(pendingTransaction.getDateTime()).isEqualTo("2020-12-01T00:35:39Z[Europe/London]");
        ExtendedTransactionDTO extendedTransaction = pendingTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-12-01T00:35:39Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-12-01T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("0.09");
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("INTEREST PAID");
        pendingTransaction.validate();

        ProviderTransactionDTO bookedTransaction = transactions.get(1);
        assertThat(bookedTransaction.getAmount()).isEqualTo("2000.00");
        assertThat(bookedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(bookedTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(bookedTransaction.getDateTime()).isEqualTo("2020-11-28T08:31:25Z[Europe/London]");
        extendedTransaction = bookedTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-28T08:31:25Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-11-30T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-2000.00");
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("MOBILE-CHANNEL");
        bookedTransaction.validate();
    }

    private void validateCreditCardTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(3);

        ProviderTransactionDTO transaction = transactions.get(0);
        assertThat(transaction.getAmount()).isEqualTo("72.00");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction.getDateTime()).isEqualTo("2020-11-12T20:16:38Z[Europe/London]");
        ExtendedTransactionDTO extendedTransaction = transaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-12T20:16:38Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isNull();
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("72.00");
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Payment, Thank You");
        transaction.validate();
    }

    private List<BalanceDTO> getExtendedBalancesForCurrentAccount() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.EXPECTED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("10.12")))
                .lastChangeDateTime(ZonedDateTime.parse("2020-12-01T07:35:55Z[Europe/London]"))
                .referenceDate(ZonedDateTime.parse("2020-12-01T07:35:55Z[Europe/London]"))
                .build());
        return balanceList;
    }


    private List<BalanceDTO> getExtendedBalancesForSecondCurrentAccount() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_AVAILABLE)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("9.12")))
                .lastChangeDateTime(ZonedDateTime.parse("2020-12-01T07:35:55Z[Europe/London]"))
                .referenceDate(ZonedDateTime.parse("2020-12-01T07:35:55Z[Europe/London]"))
                .build());
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.CLOSING_BOOKED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("11.12")))
                .lastChangeDateTime(ZonedDateTime.parse("2020-12-01T07:35:55Z[Europe/London]"))
                .referenceDate(ZonedDateTime.parse("2020-12-01T07:35:55Z[Europe/London]"))
                .build());
        return balanceList;
    }

    private List<BalanceDTO> getExtendedBalancesForCreditCard() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_CLEARED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("10.20")))
                .lastChangeDateTime(ZonedDateTime.parse("2020-12-01T07:38:17Z[Europe/London]"))
                .referenceDate(ZonedDateTime.parse("2020-12-01T07:38:17Z[Europe/London]"))
                .build());
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_BOOKED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("-10.20")))
                .lastChangeDateTime(ZonedDateTime.parse("2020-12-01T07:38:17Z[Europe/London]"))
                .referenceDate(ZonedDateTime.parse("2020-12-01T07:38:17Z[Europe/London]"))
                .build());
        return balanceList;
    }

    private Map<String, Object> getExpectedClaimsMap(String clientId,
                                                     String redirectUrl,
                                                     String state,
                                                     String consentId) {
        JwtClaims expectedClaims = new JwtClaims();
        expectedClaims.setIssuer("someClientId");
        expectedClaims.setClaim(RESPONSE_TYPE, "code id_token");
        expectedClaims.setClaim(CLIENT_ID, clientId);
        expectedClaims.setClaim(REDIRECT_URI, redirectUrl);
        expectedClaims.setClaim(SCOPE, "openid accounts");
        expectedClaims.setClaim(STATE, state);
        expectedClaims.setClaim(NONCE, state);
        expectedClaims.setClaim(MAX_AGE, TimeUnit.DAYS.toSeconds(1));
        expectedClaims.setClaim("acr_values", "urn:openbanking:psd2:sca urn:openbanking:psd2:ca");

        Map<String, Object> claimsObject = new LinkedHashMap<>();
        Map<String, Object> userInfo = new LinkedHashMap<>();
        Map<String, Object> idToken = new LinkedHashMap<>();
        Map<String, Object> openBankingIntentId = new LinkedHashMap<>();
        Map<String, Object> acr = new LinkedHashMap<>();
        acr.put("essential", true);
        acr.put("values", Arrays.asList("urn:openbanking:psd2:sca", "urn:openbanking:psd2:ca"));
        openBankingIntentId.put("value", consentId);
        openBankingIntentId.put("essential", true);
        userInfo.put("openbanking_intent_id", openBankingIntentId);
        idToken.put("openbanking_intent_id", openBankingIntentId);
        idToken.put("acr", acr);
        claimsObject.put("userinfo", userInfo);
        claimsObject.put("id_token", idToken);
        expectedClaims.setClaim("claims", claimsObject);

        return expectedClaims.getClaimsMap();
    }
}
