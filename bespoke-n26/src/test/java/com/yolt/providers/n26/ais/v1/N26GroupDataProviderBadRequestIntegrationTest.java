package com.yolt.providers.n26.ais.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.n26.TestApp;
import com.yolt.providers.n26.common.auth.N26GroupPKCE;
import com.yolt.providers.n26.common.dto.N26GroupProviderState;
import com.yolt.providers.n26.common.service.mapper.N26GroupProviderStateMapper;
import com.yolt.providers.n26.n26.config.N26DataProviderV1;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.n26.common.auth.N26GroupAuthenticationMeansProducerV1.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("n26")
@AutoConfigureWireMock(stubs = "classpath:/mappings/n26/bad-request", httpsPort = 0, port = 0)
public class N26GroupDataProviderBadRequestIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Date UPDATED_DATE = parseDate("2020-01-01");
    private static final Date EXPIRATION_DATE = parseDate("2020-01-02");
    private static final String CONSENT_ID = "16640bfe-9a98-441a-8380-c568976eee4a";
    private static final String REDIRECT_URI = "https://www.yolt.com/callback";
    private static final String REQUEST_ID = "56640bfe-9a98-441a-8380-c568976eee4a";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private N26DataProviderV1 dataProvider;

    @Autowired
    private Clock clock;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private N26GroupProviderStateMapper providerStateMapper;
    private OAuth2ProofKeyCodeExchange codeExchange;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        providerStateMapper = new N26GroupProviderStateMapper(new ObjectMapper(), clock);
        codeExchange = new N26GroupPKCE().createRandomS256();

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "client-id"));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
    }

    @Test
    void shouldThrowTokenInvalidExceptionForOnUserSiteDeleteWhenCannotDeleteConsent() {
        // given
        UrlOnUserSiteDeleteRequest request = new UrlOnUserSiteDeleteRequestBuilder()
                .setAccessMeans(createAccessMeansDTO())
                .setAuthenticationMeans(authenticationMeans)
                .setExternalConsentId(UUID.randomUUID().toString())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(request);

        // then
        Assertions.assertThatThrownBy(onUserSiteDeleteCallable)
                .isInstanceOf(TokenInvalidException.class);
        WireMock.verify(1, WireMock.deleteRequestedFor(WireMock.urlPathEqualTo("/v1/berlin-group/v1/consents/16640bfe-9a98-441a-8380-c568976eee4a")));
    }

    private AccessMeansDTO createAccessMeansDTO() {
        N26GroupProviderState providerState = createProviderState();
        providerState.setTokens(ACCESS_TOKEN, REFRESH_TOKEN);
        providerState.setConsentId(CONSENT_ID);
        return new AccessMeansDTO(USER_ID, providerStateMapper.toJson(providerState), UPDATED_DATE, EXPIRATION_DATE);
    }

    private N26GroupProviderState createProviderState() {
        return new N26GroupProviderState(codeExchange, REDIRECT_URI, REQUEST_ID, clock);
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }
}
