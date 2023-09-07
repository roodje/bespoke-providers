package com.yolt.providers.stet.bnpparibasgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupDataProvider;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupTestConfig;
import com.yolt.providers.stet.bnpparibasgroup.bnpparibas.BnpParibasDataProviderV6;
import com.yolt.providers.stet.bnpparibasgroup.common.BnpParibasGroupTestsConstants;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAccessMeans;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAuthenticationMeans;
import com.yolt.providers.stet.bnpparibasgroup.hellobank.HelloBankDataProviderV6;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BnpParibasGroupTestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bnpparibasgroup")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/bnpparibasgroup/ais/empty-body"}, httpsPort = 0, port = 0)
class BnpParibasGroupDataProvidersEmptyAccountResponseIntegrationTest {
    private final BnpParibasGroupSampleAccessMeans sampleAccessMeans = new BnpParibasGroupSampleAccessMeans();
    private final BnpParibasGroupSampleAuthenticationMeans sampleAuthenticationMeans = new BnpParibasGroupSampleAuthenticationMeans();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private BnpParibasDataProviderV6 bnpParibasDataProvider;

    @Autowired
    private HelloBankDataProviderV6 helloBankDataProvider;

    @Autowired
    private RestTemplateManager restTemplateManagerMock;

    @Autowired
    private Signer signerMock;

    public Stream<BnpParibasGroupDataProvider> getProviders() {
        return Stream.of(bnpParibasDataProvider, helloBankDataProvider);
    }

    @BeforeEach
    void setup() throws IOException, URISyntaxException {

        authenticationMeans = sampleAuthenticationMeans.getBnpSampleAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnEmptyAccountListWhenThereIsNoResponseBody(BnpParibasGroupDataProvider dataProvider) throws JsonProcessingException, TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(BnpParibasGroupTestsConstants.TRANSACTIONS_FETCH_START_TIME)
                .setAccessMeans(sampleAccessMeans.createAccessMeans(BnpParibasGroupTestsConstants.ACCESS_TOKEN_EMPTY, BnpParibasGroupTestsConstants.USER_ID))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(urlFetchDataRequest);

        // then
        assertThat(response.getAccounts()).isEmpty();
    }

}
