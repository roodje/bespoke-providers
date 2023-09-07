package com.yolt.providers.openbanking.ais.nationwide.ais.v10;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.nationwide.NationwideApp;
import com.yolt.providers.openbanking.ais.nationwide.NationwideDataProviderV10;
import com.yolt.providers.openbanking.ais.nationwide.NationwideSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request to accounts returned 403.
 * This means that access for requested data is forbidden, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 * Covered flows:
 * - fetching accounts
 */
@SpringBootTest(classes = {NationwideApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/nationwide/ais-3.1.2/accounts-403", httpsPort = 0, port = 0)
@ActiveProfiles("nationwide")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NationwideDataProviderFetchData403IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";

    @Autowired
    @Qualifier("NationwideDataProviderV11")
    private NationwideDataProviderV10 nationwideDataProviderV11;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(nationwideDataProviderV11);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowTokenInvalidExceptionWhenResponseStatusIs403(UrlDataProvider provider) throws Exception {
        //given
        AccessMeansState<AccessMeans> token = new AccessMeansState<>(new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL),
                List.of("ReadParty",
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadDirectDebits",
                        "ReadProducts",
                        "ReadStandingOrdersDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"));
        String serializedAccessMeans = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(new NationwideSampleAuthenticationMeans().getAuthenticationMeans())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> "12345"))
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }
}