package com.yolt.providers.knabgroup.knab.v2.sadflow.missingrefreshtoken;

import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.knabgroup.TestApp;
import com.yolt.providers.knabgroup.TestRestTemplateManager;
import com.yolt.providers.knabgroup.TestSigner;
import com.yolt.providers.knabgroup.common.KnabGroupDataProviderV2;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/sadflowv2/", httpsPort = 0, port = 0)
public class MissingRefreshTokenIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("fdbc609b-ec60-4ddf-a19a-5223c8b5b100");

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    @Qualifier("KnabDataProviderV2")
    private KnabGroupDataProviderV2 urlDataProvider;

    private RestTemplateManager restTemplateManager;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @BeforeEach
    public void beforeEach() {
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        signer = new TestSigner();
    }

    @Test
    public void shouldThrowTokenInvalidExceptionExceptionWhenRefreshTokenIsMissing() {
        // given
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(createAccessMeansDto())
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> urlDataProvider.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Missing refresh token");
    }

    private AccessMeansDTO createAccessMeansDto() {
        String SERIALIZED_ACCESS_MEANS = "{\"accessToken\":\"userAccessToken\",\"refreshToken\":\"\",\"tokenType\":\"Bearer\",\"expiryTimestamp\":1595848039000,\"scope\":\"psd2 offline_access AIS:userConsentId\"}";
        Date UNUSED_DATE_IN_PROVIDERS_SERVICE = new Date();
        return new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, UNUSED_DATE_IN_PROVIDERS_SERVICE, UNUSED_DATE_IN_PROVIDERS_SERVICE);
    }
}
