package com.yolt.providers.stet.bnpparibasfortisgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.stet.SignerMock;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.BnpParibasFortisDataProviderV2;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.config.BnpParibasFortisProperties;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains missing data flows occurring in BNP Paribas Fortis group providers. As a business decision provider implementation should return dto as best effort
 * <p>
 * Disclaimer: as all providers in BNP Paribas Fortis group are the same from code and stubs perspective (then only difference is configuration)
 * we are using {@link BnpParibasFortisDataProviderV2} for testing, but this covers all providers from BNP Paribas Fortis group
 * <p>
 * Covered flows:
 * - fetching accounts, balances, transactions when some data are missing
 * <p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/bnpparibasfortis-2.0.1/missing-data/", httpsPort = 0, port = 0)
@Import(BnpParibasFortisTestConfig.class)
@ActiveProfiles("bnpparibasfortis")
class BnpParibasFortisGroupDataProviderMissingInformationIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String ACCESS_TOKEN = "b343aa01-ea3c-4fe5-9658-944c82cb7683";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    private final BnpParibasFortisGroupSampleMeans sampleAuthenticationMeans = new BnpParibasFortisGroupSampleMeans();
    private final RestTemplateManager restTemplateManagerMock = new SimpleRestTemplateManagerMock();
    private final SignerMock signerMock = new SignerMock();

    @Autowired
    private BnpParibasFortisDataProviderV2 dataProvider;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("BnpParibasFortisStetProperties")
    private BnpParibasFortisProperties properties;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getConfiguredAuthMeans();
    }

    @Test
    void shouldSuccessfullyFetchData() throws ProviderFetchDataException, TokenInvalidException {
        // given
        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(USER_ID, BnpParibasFortisGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, properties, ACCESS_TOKEN), new Date(), new Date())
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(fetchDataRequest);

        // then
        ProviderAccountDTO accountWithMissingData = response.getAccounts().get(0);
        assertThat(accountWithMissingData.getCurrentBalance()).isNull();
        assertThat(accountWithMissingData.getAvailableBalance()).isNull();
        assertThat(accountWithMissingData.getCurrency()).isNull();
    }
}
