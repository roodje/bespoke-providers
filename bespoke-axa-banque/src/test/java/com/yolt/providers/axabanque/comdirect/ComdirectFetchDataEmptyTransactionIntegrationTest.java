package com.yolt.providers.axabanque.comdirect;

import com.yolt.providers.axabanque.common.fixtures.AuthMeansFixture;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/v1/fetchdata/comdirect/emptytransaction", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComdirectFetchDataEmptyTransactionIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TRANSPORT_KEY_ID_VALUE = UUID.randomUUID().toString();
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = AuthMeansFixture.getAuthMeansMap(TRANSPORT_KEY_ID_VALUE);
    private static final String PST_IP_ADDRESS = "127.0.1.2";
    private static final ZoneId zoneId = ZoneId.of("Europe/Berlin");

    @Value("${wiremock.server.port}")
    private int port;

    @Autowired
    @Qualifier("ComdirectDataProviderV1")
    private UrlDataProvider comdirectDataProviderV1;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(comdirectDataProviderV1);
    }

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Mock
    private Signer signer;

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldSuccessfullyFetchDataAndMapDataCorrectlyWhenEmptyTransactionListIsReturned(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        long consentGeneratedNow = Instant.now(Clock.system(zoneId)).toEpochMilli();
        String accessMeans = "{\"baseRedirectUri\":\"https:baseUri.com\",\"providerState\":{\"codeVerifier\":\"codeVerifier\",\"code\":\"code\",\"consentId\":\"consentId\",\"traceId\":\"traceId\",\"consentGeneratedAt\":" + consentGeneratedNow + "},\"accessToken\":{\"expiresIn\":900,\"refreshToken\":\"THE-REFRESH-TOKEN\",\"scope\":\"token scope\",\"tokenType\":\"Bearer\",\"token\":\"THE-ACCESS-TOKEN\"}}";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setSigner(signer)
                .setTransactionsFetchStartTime(ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PST_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then

        assertThat(response.getAccounts()).hasSize(1);
        //account0
        ProviderAccountDTO account0 = response.getAccounts().stream()
                .filter(a -> a.getAccountId().equals("8EAAC7F4F3E75F786FB71B7C111111"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No account found"));
        assertThat(account0.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(account0.getAvailableBalance()).isEqualTo("500.0");
        assertThat(account0.getCurrentBalance()).isEqualTo("500.0");
        assertThat(account0.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account0.getCreditCardData().getAvailableCreditAmount()).isEqualTo("500.0");
        assertThat(account0.getTransactions()).isEmpty();
    }
}

