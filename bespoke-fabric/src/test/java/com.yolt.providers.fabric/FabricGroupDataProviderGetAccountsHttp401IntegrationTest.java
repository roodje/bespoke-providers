package com.yolt.providers.fabric;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.fabric.common.FabricGroupDataProviderV1;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = AppConf.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs", httpsPort = 0, port = 0)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("fabric")
public class FabricGroupDataProviderGetAccountsHttp401IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate CONSENT_DATE = LocalDate.of(2022, 01, 01);
    private static final Instant FETCH_START_TIME = CONSENT_DATE.minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("BancaSellaDataProviderV1")
    private FabricGroupDataProviderV1 bancaSellaDataProvider;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Stream<UrlDataProvider> getAllFabricGroupDataProviders() {
        return Stream.of(bancaSellaDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldThrowTokenInvalidExceptionOnHttp401(FabricGroupDataProviderV1 dataProvider) {
        // given
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        String accessMeans = "{\"consentId\":\"401\",\"consentGeneratedAt\":1, \"consentValidTo\":\"2022-01-01\"}";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(accessMeansDTO)
                .setPsuIpAddress("127.0.0.1")
                .setTransactionsFetchStartTime(FETCH_START_TIME)
                .build();

        // when
        assertThatThrownBy(() -> dataProvider.fetchData(request))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }
}

