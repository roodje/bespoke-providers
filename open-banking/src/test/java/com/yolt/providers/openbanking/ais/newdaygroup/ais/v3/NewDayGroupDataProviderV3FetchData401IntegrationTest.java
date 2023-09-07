package com.yolt.providers.openbanking.ais.newdaygroup.ais.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupApp;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.newdaygroup.amazoncreditcard.AmazonCreditCardDataProviderV3;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This tests covers the handling for HTTP-401 UNAUTHORIZED response on the accounts endpoint
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {NewDayGroupApp.class,
        OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("newdaygroup-v1")
@AutoConfigureWireMock(stubs = "classpath:/stubs/newdaygroup/ais-3.1/accounts-401/", httpsPort = 0, port = 0)
public class NewDayGroupDataProviderV3FetchData401IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String REDIRECT_URL = "https://www.yolt.com/test";
    private static AccessMeans ACCESS_MEANS;

    private RestTemplateManager restTemplateManagerMock;

    @Autowired
    @Qualifier("AmazonCreditCardDataProviderV3")
    private AmazonCreditCardDataProviderV3 amazonDataProvider;

    @Autowired
    @Qualifier("AquaCreditCardDataProviderV3")
    private GenericBaseDataProvider aquaDataProvider;

    @Autowired
    @Qualifier("ArgosDataProviderV3")
    private GenericBaseDataProvider argosDataProvider;

    @Autowired
    @Qualifier("HouseOfFaserDataProviderV3")
    private GenericBaseDataProvider fraserDataProvider;

    @Autowired
    @Qualifier("MarblesDataProviderV3")
    private GenericBaseDataProvider marblesDataProvider;

    @Autowired
    @Qualifier("DebenhamsDataProviderV3")
    private GenericBaseDataProvider debenhamsDataProvider;

    public Stream<UrlDataProvider> getNewDayDataProviders() {
        return Stream.of(amazonDataProvider,
                aquaDataProvider,
                argosDataProvider,
                fraserDataProvider,
                marblesDataProvider,
                debenhamsDataProvider);
    }

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void setup() {
        Instant now = Instant.now();
        ACCESS_MEANS = new AccessMeans(
                now,
                USER_ID,
                "fake-access-token",
                "fake-refresh-token",
                Date.from(now.plus(1, ChronoUnit.DAYS)),
                Date.from(now),
                REDIRECT_URL);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new NewDayGroupSampleAuthenticationMeansV2().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), any()))
                .thenReturn(new NewDayGroupJwsSigningResult());
    }

    @ParameterizedTest
    @MethodSource("getNewDayDataProviders")
    public void shouldThrowTokenInvalidExceptionOnHttp401(UrlDataProvider dataProvider) {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(), new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        assertThatThrownBy(() -> dataProvider.fetchData(urlFetchData))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    private String getSerializedAccessMeans() {
        try {
            return objectMapper.writeValueAsString(ACCESS_MEANS);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize oAuthToken", e);
        }
    }
}