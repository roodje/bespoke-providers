package com.yolt.providers.stet.lclgroup.lcl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Token;
import com.yolt.providers.stet.lclgroup.LclGroupTestConfig;
import com.yolt.providers.stet.lclgroup.lcl.configuration.LclStetProperties;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = LclGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lclgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/lcl/refresh-token-400", httpsPort = 0, port = 0)
public class LclDataProviderV3RefreshAccessMeansRefreshTokenExpiredIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("07a540a2-7b91-11e9-8f9e-2a86e4085a59");
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String CERTIFICATES_PATH = "certificates/fake-certificate.pem";

    private Map<String, BasicAuthenticationMean> clientConfiguration;

    @Autowired
    @Qualifier("LclDataProviderV3")
    private LclDataProviderV3 dataProvider;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signer;

    @Autowired
    private LclStetProperties properties;

    @Test
    void shouldThrowTokenInvalidException() throws JsonProcessingException {
        // given
        clientConfiguration = new LclGroupSampleAuthenticationMeans().getSampleAuthMeans();
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(createAccessMeans(createDataProviderState(ACCESS_TOKEN)))
                .setAuthenticationMeans(clientConfiguration)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Http error status code: 400 BAD_REQUEST");
    }

    private AccessMeansDTO createAccessMeans(final DataProviderState oAuthToken) throws JsonProcessingException {
        String serializedOAuthToken = new ObjectMapper().writeValueAsString(oAuthToken);
        return new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date());
    }

    private DataProviderState createDataProviderState(String accessToken) {
        DataProviderState dataProviderState = DataProviderState.authorizedProviderState(
                properties.getRegions().get(0),
                accessToken,
                REFRESH_TOKEN);
        return dataProviderState;
    }
}
