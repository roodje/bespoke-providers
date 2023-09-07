package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request to accounts returned 401.
 * This means that request is unauthorized, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Providers: ALL RBS Group
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RbsApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/ais/accounts_401/", port = 0, httpsPort = 0)
class RbsGroupDataProviderAccounts401IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Autowired
    @Qualifier("CouttsDataProviderV3")
    private RbsGroupDataProviderV5 couttsDataProvider;
    @Autowired
    @Qualifier("NatWestDataProviderV11")
    private RbsGroupDataProviderV5 natwestDataProvider;
    @Autowired
    @Qualifier("NatWestCorporateDataProviderV10")
    private RbsGroupDataProviderV5 natwestCorpoDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandDataProviderV11")
    private RbsGroupDataProviderV5 rbsDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandCorporateDataProviderV10")
    private RbsGroupDataProviderV5 rbsCorpoDataProvider;
    @Autowired
    @Qualifier("UlsterBankDataProviderV10")
    private RbsGroupDataProviderV5 ulsterDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<RbsGroupDataProviderV5> getProviders() {
        return Stream.of(natwestDataProvider, natwestCorpoDataProvider, rbsDataProvider, rbsCorpoDataProvider, ulsterDataProvider, couttsDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowExceptionWhenAccountsRequestFail(RbsGroupDataProviderV5 dataProvider) throws Exception {
        // given
        AccessMeans token = new AccessMeans(Instant.ofEpochMilli(0L),
                null,
                "test-accounts",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                null,
                null);
        String serializedAccessMeans = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis())
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(TokenInvalidException.class);
    }
}
