package com.yolt.providers.monorepogroup.olbgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.olbgroup.common.OlbGroupDataProvider;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.OlbGroupProviderState;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupProviderStateMapper;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeansProducerV1.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeansProducerV1.TRANSPORT_KEY_ID_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = OlbGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/olbgroup/ais/sad-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("olbgroup")
class OlbGroupDataProviderSadFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String CONSENT_ID = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";
    private static final String KEY_ID_VALUE = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");

    @Autowired
    @Qualifier("OlbGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("OlbDataProviderV1")
    private OlbGroupDataProvider dataProvider;

    @Autowired
    private Clock clock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private OlbGroupProviderStateMapper providerStateMapper;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), KEY_ID_VALUE));

        providerStateMapper = new OlbGroupProviderStateMapper(objectMapper);
    }


    @Test
    void shouldThrowTokenInvalidExceptionWhenUnauthorized() {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.fetchData(request);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("We are not authorized to call endpoint: HTTP 401");

    }

    private AccessMeansDTO createAccessMeansDTO() {
        OlbGroupProviderState providerState = createProviderState();
        return new AccessMeansDTO(USER_ID,
                providerStateMapper.toJson(providerState),
                Date.from(LocalDate.now(clock).atStartOfDay(ZONE_ID).toInstant()),
                Date.from(LocalDate.now(clock).plusDays(89).atStartOfDay(ZONE_ID).toInstant()));
    }

    private OlbGroupProviderState createProviderState() {
        return new OlbGroupProviderState(CONSENT_ID);
    }
}