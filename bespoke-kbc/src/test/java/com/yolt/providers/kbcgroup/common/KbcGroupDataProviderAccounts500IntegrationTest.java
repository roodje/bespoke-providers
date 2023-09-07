package com.yolt.providers.kbcgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.kbcgroup.FakeRestTemplateManager;
import com.yolt.providers.kbcgroup.KbcGroupSampleAuthenticationMeans;
import com.yolt.providers.kbcgroup.KbcGroupTestApp;
import com.yolt.providers.kbcgroup.cbcbank.CbcBankDataProvider;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupAccessMeans;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupTokenResponse;
import com.yolt.providers.kbcgroup.kbcbank.KbcBankDataProvider;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = KbcGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/kbcgroup/2.0.6/ais/v1/accounts_500/", httpsPort = 0, port = 0)
@ActiveProfiles("kbcgroup")
class KbcGroupDataProviderAccounts500IntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "123.45.67.89";
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = KbcGroupSampleAuthenticationMeans.get();
    private static final String TEST_CONSENT_ID = "test-consent-id";
    private static final String TEST_REDIRECT_URL = "https://example.com/callback";

    @Autowired
    @Qualifier("KbcGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private KbcBankDataProvider kbcBankDataProvider;

    @Autowired
    private CbcBankDataProvider cbcBankDataProvider;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    Stream<KbcGroupDataProvider> kbcGroupDataProviders() {
        return Stream.of(kbcBankDataProvider, cbcBankDataProvider);
    }

    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void setUp() {
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @ParameterizedTest
    @MethodSource("kbcGroupDataProviders")
    void shouldThrowProviderFetchDataExceptionWhen500IsReturnedFromAccountsEndpoint(KbcGroupDataProvider providerUnderTest) {
        // given
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(Instant.now())
                .setUserId(TEST_USER_ID)
                .build();

        // when
        assertThatThrownBy(() -> providerUnderTest.fetchData(urlFetchDataRequest)).isInstanceOf(ProviderFetchDataException.class);
    }

    @SneakyThrows
    private AccessMeansDTO getAccessMeans() {
        KbcGroupTokenResponse token = KbcGroupTokenResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .expiresIn(3600)
                .build();
        KbcGroupAccessMeans kbcGroupAccessMeans = new KbcGroupAccessMeans(token, TEST_REDIRECT_URL, TEST_CONSENT_ID);

        return new AccessMeansDTO(TEST_USER_ID,
                objectMapper.writeValueAsString(kbcGroupAccessMeans),
                new Date(),
                new Date());
    }
}