package com.yolt.providers.stet.creditagricolegroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.CreditAgricoleDataProviderV10;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.config.CreditAgricoleProperties;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.Region;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CreditAgricoleGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("creditagricole")
@AutoConfigureWireMock(stubs = "classpath:/stubs/creditagricole/ais/refresh-token-400", httpsPort = 0, port = 0)
class CreditAgricoleGroupDataProviderRefreshTokenHttp400IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "c02b1146-dddc-4903-8d6a-d7cf8309ba5c";
    private static final String REFRESH_TOKEN = "c3c0dd0c-b2da-4c8f-ab6e-72cf45451412";
    private static final String SELECTED_REGION_CODE = "CAM_ALPES_PROVENCE";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signer;

    @Autowired
    @Qualifier("CreditAgricoleStetProperties")
    private CreditAgricoleProperties creditAgricoleProperties;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("CreditAgricoleDataProviderV10")
    private CreditAgricoleDataProviderV10 creditAgricoleDataProvider;

    private Stream<Arguments> getDataProviders() {
        return Stream.of(Arguments.of(creditAgricoleDataProvider, creditAgricoleProperties));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowTokenInvalidExceptionWhenReceivedHttp400(UrlDataProvider dataProvider,
                                                             DefaultProperties properties) {
        // given
        Region selectedRegion = properties.getRegionByCode(SELECTED_REGION_CODE);
        String jsonProviderState = CreditAgricoleGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, selectedRegion, ACCESS_TOKEN, REFRESH_TOKEN);

        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowingCallable throwingCallable = () -> dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(TokenInvalidException.class);
    }
}
