package com.yolt.providers.openbanking.ais.monzogroup.ais.V5;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoApp;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains server error flow occurring in Monzo bank provider.
 */
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/monzogroup/ob_3.1/ais/account-access-consents-delete-500/", httpsPort = 0, port = 0)
@ActiveProfiles("monzogroup")
public class MonzoDataProviderV5UserSiteDeleteServerErrorTest {

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("MonzoDataProviderV5")
    private GenericBaseDataProvider monzoDataProvider;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new MonzoSampleTypedAuthMeansV2().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "93bac548-d2de-4546-b106-880a5018460d");
    }

    @Test
    public void shouldThrowHttpServerErrorExceptionInternalServerErrorWhenOnUserSiteDeleteFailed() {
        // given
        String externalConsentId = "8aeb8a48-6559-4b0e-9b8a-caab09b346c9";

        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        ThrowingCallable fetchDataCallable = () -> monzoDataProvider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(HttpServerErrorException.InternalServerError.class);
    }
}
