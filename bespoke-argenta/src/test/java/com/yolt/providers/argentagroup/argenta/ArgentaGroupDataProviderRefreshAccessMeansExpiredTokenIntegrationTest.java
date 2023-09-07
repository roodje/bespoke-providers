package com.yolt.providers.argentagroup.argenta;

import com.yolt.providers.argentagroup.ArgentaGroupTestApp;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ActiveProfiles("argenta")
@SpringBootTest(classes = {ArgentaGroupTestApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/argenta/ais-1.5/refresh-access-means-expired-refresh-token"}, httpsPort = 0, port = 0)
class ArgentaGroupDataProviderRefreshAccessMeansExpiredTokenIntegrationTest {


    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("argentaDataProviderV1")
    private UrlDataProvider dataProvider;


    @Test
    void shouldRefreshAccessMeans() throws IOException, URISyntaxException {
        // given
        var testUserId = UUID.randomUUID();

        var request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(
                        testUserId,
                        getSerializedAccessMeans(),
                        new Date(),
                        new Date()
                ).build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.refreshAccessMeans(request);

        // then
        assertThatThrownBy(throwingCallable).isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Refresh token expired: HTTP 400");
    }

    private String getSerializedAccessMeans() {
        return """
                 {
                     "accessToken":"TEST_ACCESS_TOKEN",
                     "refreshToken":"TEST_REFRESH_TOKEN",
                     "scope":"AIS",
                     "expiresIn":"600"
                }
                         """;
    }
}