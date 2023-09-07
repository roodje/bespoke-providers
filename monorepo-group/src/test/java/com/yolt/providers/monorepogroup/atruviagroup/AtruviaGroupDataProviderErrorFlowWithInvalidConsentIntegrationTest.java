package com.yolt.providers.monorepogroup.atruviagroup;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.monorepogroup.TestSigner;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaAccessMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID_HEADER_STRING;
import static com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeansFactory.CLIENT_SIGNING_CERTIFICATE_NAME;
import static com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeansFactory.CLIENT_SIGNING_KEY_ID_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@SpringBootTest(classes = AtruviaGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/atruviagroup/ais/error-flow-400-invalid-consent/", httpsPort = 0, port = 0)
@ActiveProfiles("atruviagroup")
class AtruviaGroupDataProviderErrorFlowWithInvalidConsentIntegrationTest extends AtruviaGroupDataProviderIntegrationTestBase {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String KEY_ID_VALUE = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Clock clock;

    @Autowired
    @Qualifier("VolksbankenRaiffeisenProvider")
    private UrlDataProvider dataProvider;

    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID_HEADER_STRING.getType(), KEY_ID_VALUE));
        authenticationMeans.put(CLIENT_SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), pemCertificate));
        signer = new TestSigner();
    }

    @Test
    void shouldFetchData() {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(prepareExpectedAccessMeans())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .setSigner(signer)
                .build();

        // when
        ThrowableAssert.ThrowingCallable call = () -> dataProvider.fetchData(request);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("Consent is invalid.");
    }

    private AccessMeansDTO prepareExpectedAccessMeans() {
        var expectedAuthenticationMean = toAccessMeansString(new AtruviaAccessMeans("1234-wertiq-983", "82064188"));
        return new AccessMeansDTO(USER_ID, expectedAuthenticationMean, Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plus(89, ChronoUnit.DAYS)));
    }
}