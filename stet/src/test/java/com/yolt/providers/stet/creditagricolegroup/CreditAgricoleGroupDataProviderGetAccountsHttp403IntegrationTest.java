package com.yolt.providers.stet.creditagricolegroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.CreditAgricoleDataProviderV10;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.config.CreditAgricoleProperties;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.Region;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CreditAgricoleGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("creditagricole")
@AutoConfigureWireMock(stubs = "classpath:/stubs/creditagricole/ais/accounts-403", httpsPort = 0, port = 0)
class CreditAgricoleGroupDataProviderGetAccountsHttp403IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "2acc9055-3078-453b-9f07-adde186e13e5";
    private static final String SELECTED_REGION_CODE = "CAM_ALPES_PROVENCE";

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
    void shouldThrowBackPressureExceptionWhenReceivedHttp403WithoutPsuIpAddress(UrlDataProvider dataProvider,
                                                                                DefaultProperties properties) {
        // given
        Region selectedRegion = properties.getRegionByCode(SELECTED_REGION_CODE);
        String jsonProviderState = CreditAgricoleGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, selectedRegion, ACCESS_TOKEN);

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(LocalDate.parse("2019-12-31").atStartOfDay(ZoneId.of("Europe/Paris")).toInstant())
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.fetchData(request);

        // then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(BackPressureRequestException.class);
    }
}
