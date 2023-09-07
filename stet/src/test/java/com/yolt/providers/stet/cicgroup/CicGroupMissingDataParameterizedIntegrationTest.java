package com.yolt.providers.stet.cicgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.stet.cicgroup.beobank.BeobankDataProviderV1;
import com.yolt.providers.stet.cicgroup.cic.CicDataProviderV5;
import com.yolt.providers.stet.cicgroup.cic.config.CicProperties;
import com.yolt.providers.stet.cicgroup.creditmutuel.CreditMutuelDataProviderV7;
import com.yolt.providers.stet.generic.GenericOnboardingDataProvider;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CicGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/cic/ais/missing-data", httpsPort = 0, port = 0)
@ActiveProfiles("cic")
public class CicGroupMissingDataParameterizedIntegrationTest {

    // Auxiliary constants
    private static final String YOLT_REDIRECT_URI = "https://www.yolt.com/callback";
    private static final String YOLT_REDIRECT_URI_WITH_PARAMS = "http://yolt.com?code=authorization_code&state=uniquestring";
    private static final String CODE_VERIFIER = "6965646a-e758-4478-8b22-312a96472b856965646a-e758-4478-8b22-312a96472b85";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final Date DATE = new Date();

    // Auxiliary objects
    private RestTemplateManager restTemplateManagerMock = new SimpleRestTemplateManagerMock();
    private Map<String, BasicAuthenticationMean> SAMPLE_BASIC_AUTHENTICATION_MEANS = new CicGroupSampleAuthenticationMeans().getBasicAuthenticationMeans();

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private CicProperties properties;

    @Autowired
    @Qualifier("CicDataProviderV5")
    private CicDataProviderV5 cicDataProvider;

    @Autowired
    @Qualifier("CreditMutuelDataProviderV7")
    CreditMutuelDataProviderV7 creditMutuelDataProvider;

    @Autowired
    @Qualifier("BeobankDataProviderV1")
    BeobankDataProviderV1 beobankDataProvider;

    private Stream<GenericOnboardingDataProvider> getCicGroupDataProviders() {
        return Stream.of(cicDataProvider, creditMutuelDataProvider, beobankDataProvider);
    }

    @Mock
    private Signer signer;

    @ParameterizedTest
    @MethodSource("getCicGroupDataProviders")
    public void shouldSuccessfullyFetchData(UrlDataProvider dataProvider) throws ProviderFetchDataException, TokenInvalidException {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManagerMock)
                .setRedirectUrlPostedBackFromSite(YOLT_REDIRECT_URI_WITH_PARAMS)
                .setBaseClientRedirectUrl(YOLT_REDIRECT_URI)
                .setProviderState(CicGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, properties, CODE_VERIFIER))
                .build();
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(urlCreateAccessMeans);
        String accessToken = accessMeansOrStepDTO.getAccessMeans().getAccessMeans();

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, accessToken, DATE, DATE);

        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("1.1.1.1")
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        // account
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(1);
        ProviderAccountDTO account = accounts.get(0);
        assertThat(account.getYoltAccountType()).isNull();
        assertThat(account.getCurrency()).isNull();
        assertThat(account.getAvailableBalance()).isNull();
        assertThat(account.getCurrentBalance()).isNull();
        //transactions
        assertThat(account.getTransactions()).hasSize(1);
        assertThat(account.getTransactions().get(0).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account.getTransactions().get(0).getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(account.getTransactions().get(0).getAmount()).isEqualTo("112.25");
    }
}
