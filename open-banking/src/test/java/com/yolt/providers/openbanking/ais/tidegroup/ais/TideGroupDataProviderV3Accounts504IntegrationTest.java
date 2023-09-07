package com.yolt.providers.openbanking.ais.tidegroup.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.tidegroup.TideGroupApp;
import com.yolt.providers.openbanking.ais.tidegroup.TideGroupSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.tidegroup.common.TideGroupDataProviderV2;
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
 * This test contains case according to documentation, when request to accounts returned 504.
 * This means that there are some server issues on bank side, thus we <b>DON't</b> want to force user to fill a consent (so throw {@link ProviderFetchDataException})
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TideGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("tidegroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/tidegroup/ob_3.1.1/ais/v2/accounts_504/", port = 0, httpsPort = 0)
public class TideGroupDataProviderV3Accounts504IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Autowired
    @Qualifier("TideDataProviderV3")
    private TideGroupDataProviderV2 tideDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<TideGroupDataProviderV2> getProviders() {
        return Stream.of(tideDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowExceptionWhenAccountsRequestFail(TideGroupDataProviderV2 subject) throws Exception {
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
                .setUserId(USER_ID)
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(TideGroupSampleTypedAuthMeansV2.getAuthenticationMeans())
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> subject.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(ProviderFetchDataException.class);
    }
}
