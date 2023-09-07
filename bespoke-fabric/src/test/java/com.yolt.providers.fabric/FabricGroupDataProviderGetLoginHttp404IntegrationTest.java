package com.yolt.providers.fabric;

import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.fabric.common.FabricGroupDataProviderV1;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = AppConf.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs", httpsPort = 0, port = 0)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("fabric")
public class FabricGroupDataProviderGetLoginHttp404IntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("BancaSellaDataProviderV1")
    private FabricGroupDataProviderV1 bancaSellaDataProvider;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Stream<UrlDataProvider> getAllFabricGroupDataProviders() {
        return Stream.of(bancaSellaDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldTrowGetLoginInfoUrlFailedExceptionWhenStatus404CallingForConsent(FabricGroupDataProviderV1 dataProvider) {
        // given
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        String state = "11a1aaa1-aa1a-11a1-a111-a1a11a11aa11";
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(state)
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.2")
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        ThrowableAssert.ThrowingCallable throwable = () -> dataProvider.getLoginInfo(request);

        //then
        assertThatThrownBy(throwable)
                .isExactlyInstanceOf(GetLoginInfoUrlFailedException.class);
    }
}

