package com.yolt.providers.openbanking.ais.santander.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.santander.SantanderApp;
import com.yolt.providers.openbanking.ais.santander.SantanderSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.santander.dto.SantanderAccessMeansV2;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request acquiring accounts fail due to 400.
 * This means that there was a mistake in our request, thus we can't map such account (so throw {@link ProviderFetchDataException})
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SantanderApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("santander")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/santander/ais-3.1.6/accounts-400"}, httpsPort = 0, port = 0)
class SantanderDataProviderFetchData400IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Signer SIGNER = new SignerMock();

    private static AccessMeansState<SantanderAccessMeansV2> token;

    private RestTemplateManagerMock restTemplateManagerMock;
    private String requestTraceId;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("SantanderDataProviderV17")
    private GenericBaseDataProviderV2 santanderDataProviderV17;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setup() {
        token = new AccessMeansState<>(new SantanderAccessMeansV2(
                Instant.now(),
                USER_ID,
                "AAIkMDM5NDJmZTUtOGNiMi00NzVmLWIwMTItNDgyZjM0ZTExYzI58q0t070fBgubnd8pgwu3kCwNt91ZJhhW3wfUl2UulSRjiKcfWfQQ9J9i8OU2QOSciVIl8mQ69GO7mDZ0uEv8INrboRu4fesBmEMq7PS87O7LrN7isyqwzpjKXBZR2JJkL3nF10SuDt_l4SItojPO4",
                "qx3scq02pKLSkSJklsjDJwi8SJN82kSD44tGLSLKjsiojw89mDMUIHMDSIUyw89m2DuTlkCwRFxY0xSsKQuYAC6BinbvjksHMFIsihmsiuHMISUIW88w78SMJI8smjKMSJHKJSHMWIWSHIUGWUIgukwgjhskjshhkjsjkdhmsjkhdgshjhgsfsdfwefefwsefsegsdgsdfasjhguiynGUYFGU",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                "redirect"),
                List.of("ReadParty",
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadDirectDebits",
                        "ReadProducts",
                        "ReadStandingOrdersDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"));
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = new SantanderSampleAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowProviderFetchDataException(UrlDataProvider provider) {
        // given
        requestTraceId = "1626df30-50ad-42d8-8f39-40dd95f4b15f";
        Instant fromFetchDate = Instant.parse("2015-01-01T00:00:00Z");
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(), new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(fromFetchDate)
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        decimalFormat.setParseBigDecimal(true);

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(ProviderFetchDataException.class);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowTokenInvalidExceptionForNonAuthorizedConsent(UrlDataProvider provider) {
        // given
        requestTraceId = "1626df30-50ad-42d8-8f39-40dd95f4b15e";
        Instant fromFetchDate = Instant.parse("2015-01-01T00:00:00Z");
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(), new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(fromFetchDate)
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        decimalFormat.setParseBigDecimal(true);

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    private String getSerializedAccessMeans() {
        try {
            return objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize oAuthToken", e);
        }
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(santanderDataProviderV17);
    }
}
