package com.yolt.providers.stet.cmarkeagroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.stet.cmarkeagroup.common.CmArkeaGroupDataProvider;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.Assertions;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CmArkeaGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("cmarkeagroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/cmarkea/ais/1.4.2", httpsPort = 0, port = 0)
class CmArkeaGroupDataProviderV3HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String BASE_CLIENT_REDIRECT_URL = "http://www.yolt.com/callback";
    private final Signer signer = mock(Signer.class);

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("FortuneoDataProviderV3")
    private CmArkeaGroupDataProvider fortuneoDataProviderV3;

    @Autowired
    @Qualifier("AxaDataProviderV3")
    private CmArkeaGroupDataProvider axaDataProviderV3;

    @Autowired
    @Qualifier("AllianzBanqueDataProviderV1")
    private CmArkeaGroupDataProvider allianzBanqueDataProviderV1;

    @Autowired
    @Qualifier("MaxBankDataProviderV1")
    private CmArkeaGroupDataProvider maxBankDataProviderV1;

    @Autowired
    @Qualifier("CreditMutuelDuSudOuestDataProviderV1")
    private CmArkeaGroupDataProvider creditMutuelDuSudOuestDataProviderV1;

    @Autowired
    @Qualifier("ArkeaBanqueEntreprisesDataProviderV1")
    private CmArkeaGroupDataProvider arkeaBanqueEntreprisesDataProviderV1;

    @Autowired
    @Qualifier("ArkeaBanquePriveeDataProviderV1")
    private CmArkeaGroupDataProvider arkeaBanquePriveeDataProviderV1;

    @Autowired
    @Qualifier("ArkeaBankingServicesDataProviderV1")
    private CmArkeaGroupDataProvider arkeaBankingServicesDataProviderV1;

    @Autowired
    @Qualifier("BpeDataProviderV1")
    private CmArkeaGroupDataProvider bpeDataProviderV1;

    @Autowired
    @Qualifier("CreditMutuelDeBretagneDataProviderV1")
    private CmArkeaGroupDataProvider creditMutuelDeBretagneDataProviderV1;

    @Autowired
    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(fortuneoDataProviderV3, axaDataProviderV3, allianzBanqueDataProviderV1, maxBankDataProviderV1,
                creditMutuelDuSudOuestDataProviderV1, creditMutuelDeBretagneDataProviderV1, arkeaBanqueEntreprisesDataProviderV1,
                arkeaBanquePriveeDataProviderV1, arkeaBankingServicesDataProviderV1, bpeDataProviderV1);
    }

    @Autowired
    @Qualifier("FortuneoStetProperties")
    private DefaultProperties properties;

    @Autowired
    private Clock clock;

    @BeforeEach
    public void setUp() {
        when(signer.sign(ArgumentMatchers.any(byte[].class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(Base64.toBase64String("TEST-ENCODED-SIGNATURE".getBytes()));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnConsentPageUrl(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(CmArkeaGroupSampleMeans.createTestAuthenticationMeans())
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setState(UUID.randomUUID().toString())
                .build();

        // when
        RedirectStep step = (RedirectStep) dataProvider.getLoginInfo(request);

        String loginUrl = step.getRedirectUrl();

        // then
        Map<String, String> queryParams = getQueryParamsFromUrl(loginUrl);
        assertThat(queryParams.get("response_type")).isEqualTo("code");
        assertThat(queryParams.get("scope")).isEqualTo("aisp");
        assertThat(queryParams.get("client_id")).isEqualTo("some_client_id");
        assertThat(queryParams.get("redirect_uri")).isEqualTo("http://www.yolt.com/callback");
        assertThat(queryParams.get("state")).matches("[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-4[a-zA-Z0-9]{3}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        String redirectUrl = "http://www.bogus.com/callback?code=some_code&state=06765c56-c1e6-11ea-b3de-0242ac130005";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(CmArkeaGroupSampleMeans.createTestAuthenticationMeans())
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setBaseClientRedirectUrl("https://redirect.url")
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setProviderState(CmArkeaGroupSampleMeans.createPreAuthorizedJsonProviderState(objectMapper, properties))
                .build();

        // when
        AccessMeansOrStepDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans);
        DataProviderState cmArkeaGroupAccessToken = objectMapper.readValue(newAccessMeans.getAccessMeans().getAccessMeans(), DataProviderState.class);

        // then
        assertThat(newAccessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        assertThat(cmArkeaGroupAccessToken.getAccessToken()).isEqualTo("access-token");
        assertThat(cmArkeaGroupAccessToken.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(cmArkeaGroupAccessToken.isRefreshed()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldRefreshAccessMeansSuccessfully(UrlDataProvider dataProvider) throws Exception {
        // given
        String accessMeans = "{\"expires_in\":3600,\"access_token\":\"access-token\",\"refresh_token\":\"refresh-token\", \"refreshed\":\"false\"}";

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                USER_ID,
                accessMeans,
                new Date(),
                new Date());

        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(CmArkeaGroupSampleMeans.createTestAuthenticationMeans())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .build();

        // when
        AccessMeansDTO refreshedAccessMeans = dataProvider.refreshAccessMeans(urlRefreshAccessMeans);
        DataProviderState refreshedToken = objectMapper.readValue(refreshedAccessMeans.getAccessMeans(), DataProviderState.class);

        // then
        assertThat(refreshedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(refreshedToken.getAccessToken()).isEqualTo("access-token");
        assertThat(refreshedToken.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(refreshedToken.isRefreshed()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnOneAccountWithTransactionPaginationBasedOnPsuIpAddress(UrlDataProvider dataProvider) throws ProviderFetchDataException, TokenInvalidException {
        // given
        String accessMeans = "{\"expires_in\":3600,\"access_token\":\"accessToken123456700\",\"refresh_token\":\"refresh-token\", \"refreshed\":\"false\"}";

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                USER_ID,
                accessMeans,
                new Date(),
                new Date());

        UrlFetchDataRequest request = createFetchDataRequest(accessMeansDTO);

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(request);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        assertThat(providerAccountDTO.getName()).isEqualTo("COMPTE COURANT : BERGOT FRANCIS");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("2090.27");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("2868.06");
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        List<ProviderTransactionDTO> transactions = providerAccountDTO.getTransactions();
        Assertions.assertThat(transactions).hasSize(12);
        transactions.forEach(ProviderTransactionDTO::validate);

        ProviderTransactionDTO transaction = providerAccountDTO.getTransactions().get(0);
        assertThat(transaction.getAmount()).isEqualTo("2.64");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction.getDescription()).isEqualTo("ECH PRET 0709000009102");
        assertThat(transaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction.getDateTime()).isEqualTo("2020-03-17T23:00Z[Europe/Paris]");

        ExtendedTransactionDTO extendedTransaction = transaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-03-17T23:00Z[Europe/Paris]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-03-18T00:00+01:00[Europe/Paris]");
    }

    private UrlFetchDataRequest createFetchDataRequest(AccessMeansDTO accessMeansDTO) {
        return new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(CmArkeaGroupSampleMeans.createTestAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setPsuIpAddress("127.0.0.1")
                .build();
    }

    private Map<String, String> getQueryParamsFromUrl(String url) {
        return UriComponentsBuilder.fromUriString(url)
                .build()
                .getQueryParams()
                .toSingleValueMap();
    }
}
