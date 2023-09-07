package com.yolt.providers.stet.bnpparibasgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupDataProvider;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupTestConfig;
import com.yolt.providers.stet.bnpparibasgroup.bnpparibas.BnpParibasDataProviderV6;
import com.yolt.providers.stet.bnpparibasgroup.common.BnpParibasGroupTestsConstants;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAccessMeans;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAuthenticationMeans;
import com.yolt.providers.stet.bnpparibasgroup.hellobank.HelloBankDataProviderV6;
import org.assertj.core.api.ThrowableAssert;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BnpParibasGroupTestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bnpparibasgroup")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/bnpparibasgroup/ais/refresh-token-400-invalid-grant"}, httpsPort = 0, port = 0)
class BnpParibasGroupDataProvidersError400InvalidGrantIntegrationTest {

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
    void shouldThrowTokenInvalidExceptionDuringRefreshAccessMeansWhenInvalidGrant(BnpParibasGroupDataProvider dataProvider) throws JsonProcessingException {
        // given
        UrlRefreshAccessMeansRequest accessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(sampleAccessMeans.createAccessMeans(BnpParibasGroupTestsConstants.ACCESS_TOKEN, BnpParibasGroupTestsConstants.USER_ID))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when

        ThrowableAssert.ThrowingCallable accessMeansCallable = () -> dataProvider.refreshAccessMeans(accessMeansRequest);

        // then
        assertThatThrownBy(accessMeansCallable).isInstanceOf(TokenInvalidException.class);
    }
}
