package com.yolt.providers.argentagroup.argenta;

import com.yolt.providers.argentagroup.ArgentaGroupTestApp;
import com.yolt.providers.argentagroup.SignerMock;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
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
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ActiveProfiles("argenta")
@SpringBootTest(classes = {ArgentaGroupTestApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/argenta/ais-1.5/fetch-data-accounts-http401"}, httpsPort = 0, port = 0)
class ArgentaGroupDataProviderHttp401OnGetAccountsIntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "192.168.0.1";
    private static final Signer SIGNER = new SignerMock();
    private static final Instant TEST_TRANSACTION_FETCH_START_TIME = Instant.parse("2020-12-31T00:00:00Z");

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("argentaDataProviderV1")
    private UrlDataProvider dataProvider;

    @Test
    void shouldThrowTokenInvalidExceptionWhenAccountsEndpointRespondWith401() throws IOException, URISyntaxException {
        // given
        var testUserId = UUID.randomUUID();

        var request = new UrlFetchDataRequestBuilder()
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setAccessMeans(testUserId, getSerializedAccessMeans(), new Date(), new Date())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(SIGNER)
                .setTransactionsFetchStartTime(TEST_TRANSACTION_FETCH_START_TIME)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.fetchData(request);

        // then
        assertThatThrownBy(throwingCallable).isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("We are not authorized to call endpoint: HTTP 401");
    }

    private String getSerializedAccessMeans() {
        return """
                {"accessToken":"TEST_ACCESS_TOKEN","refreshToken":"TEST_REFRESH_TOKEN","scope":"AIS","expiresIn":"600","consentId":"TEST_CONSENT_ID"}""";
    }
}