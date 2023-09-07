package com.yolt.providers.fabric;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.fabric.common.FabricGroupDataProviderV1;
import com.yolt.providers.fabric.common.model.GroupProviderState;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = AppConf.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs", httpsPort = 0, port = 0)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("fabric")
public class FabricGroupDataProviderGetLAuthorizationHttp404IntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate CONSENT_DATE = LocalDate.of(2022, 01, 01);
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("BancaSellaDataProviderV1")
    private FabricGroupDataProviderV1 bancaSellaDataProvider;

    @Autowired
    @Qualifier("FabricGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Stream<UrlDataProvider> getAllFabricGroupDataProviders() {
        return Stream.of(bancaSellaDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldThrowGetAccessTokenFailedExceptionWhenStatus404CallingForAuthorizationConsentResource(FabricGroupDataProviderV1 dataProvider) throws JsonProcessingException {
        //given
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        GroupProviderState providerState = new GroupProviderState("22222", CONSENT_DATE.toEpochSecond(LocalTime.parse("00:00:00"), ZoneOffset.UTC), CONSENT_DATE);

        //given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite("https:\\baseRedirectUri.com")
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(objectMapper.writeValueAsString(providerState))
                .build();

        //when
        ThrowableAssert.ThrowingCallable throwable = () -> dataProvider.createNewAccessMeans(request);

        //then
        assertThatThrownBy(throwable)
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }
}

