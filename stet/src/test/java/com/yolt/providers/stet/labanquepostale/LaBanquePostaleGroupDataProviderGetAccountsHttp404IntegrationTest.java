package com.yolt.providers.stet.labanquepostale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.LaBanquePostaleDataProviderV5;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.config.LaBanquePostaleProperties;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = LaBanquePostaleGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("labanquepostale")
@AutoConfigureWireMock(stubs = "classpath:/stubs/labanquepostale/ais/accounts-404", httpsPort = 0, port = 0)
class LaBanquePostaleGroupDataProviderGetAccountsHttp404IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCESS_TOKEN = "946f248b-4c5a-45d0-b6d1-c16dfcad6f82";

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signer;

    @Autowired
    @Qualifier("LaBanquePostaleStetProperties")
    private LaBanquePostaleProperties laBanquePostaleProperties;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("LaBanquePostaleDataProviderV5")
    private LaBanquePostaleDataProviderV5 laBanquePostaleDataProvider;

    private Stream<Arguments> getDataProviders() {
        return Stream.of(Arguments.of(laBanquePostaleDataProvider, laBanquePostaleProperties));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnEmptyListWhenResponseIs404OnAccountsEndpoint(UrlDataProvider dataProvider, DefaultProperties properties) throws TokenInvalidException, ProviderFetchDataException {
        // given
        String jsonProviderState = LaBanquePostaleGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, properties, ACCESS_TOKEN);

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        DataProviderResponse response =  dataProvider.fetchData(request);

        // then
        assertThat(response.getAccounts()).isEmpty();
    }
}
