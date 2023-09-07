package com.yolt.providers.bancatransilvania.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupPKCE;
import com.yolt.providers.bancatransilvania.common.domain.BancaTransilvaniaGroupProviderState;
import com.yolt.providers.bancatransilvania.common.mapper.BancaTransilvaniaGroupProviderStateMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeansProducerV1.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;

/**
 * Setup for Banca Transilvania integration tests
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bancatransilvania")
public abstract class BancaTransilvaniaGroupDataProviderTestBaseSetup {

    protected static final String PSU_IP_ADDRESS = "127.0.0.1";
    protected static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    protected static final Date UPDATED_DATE = parseDate("2020-01-01");
    protected static final Date EXPIRATION_DATE = parseDate("2020-01-02");
    protected static final String CLIENT_ID = "client-id";
    protected static final String CONSENT_ID = "100";
    protected static final String REDIRECT_URI = "https://yolt.com/callback";

    @Autowired
    @Qualifier("BancaTransilvaniaGroupObjectMapper")
    protected ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    protected RestTemplateManager restTemplateManager;

    @Autowired
    protected BancaTransilvaniaGroupDataProvider dataProvider;

    @Autowired
    protected Clock clock;

    @Mock
    protected Signer signer;

    protected Map<String, BasicAuthenticationMean> authenticationMeans;
    protected BancaTransilvaniaGroupProviderStateMapper providerStateMapper;
    protected OAuth2ProofKeyCodeExchange codeExchange;
    protected String pemCertificate;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        providerStateMapper = new BancaTransilvaniaGroupProviderStateMapper(objectMapper, clock);
        codeExchange = new BancaTransilvaniaGroupPKCE().createRandomS256();

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_NAME, new BasicAuthenticationMean(CLIENT_NAME_TYPE.getType(), "TPP Application"));
        authenticationMeans.put(CLIENT_COMPANY_NAME, new BasicAuthenticationMean(CLIENT_COMPANY_NAME_TYPE.getType(), "TPP Corporation"));
        authenticationMeans.put(CLIENT_WEBSITE_URI_NAME, new BasicAuthenticationMean(CLIENT_WEBSITE_URI_TYPE.getType(), "https://yolt.com"));
        authenticationMeans.put(CLIENT_CONTACT_NAME, new BasicAuthenticationMean(CLIENT_CONTACT_NAME_TYPE.getType(), "Contact TPP"));
        authenticationMeans.put(CLIENT_EMAIL_NAME, new BasicAuthenticationMean(CLIENT_EMAIL.getType(), "contact.tpp@test.com"));
        authenticationMeans.put(CLIENT_PHONE_NAME, new BasicAuthenticationMean(CLIENT_PHONE_TYPE.getType(), "+40700000000"));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "client-secret"));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
    }

    protected UrlFetchDataRequest buildGenericFetchDataRequest() {
        return new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();
    }

    protected AccessMeansDTO createAccessMeansDTO() {
        BancaTransilvaniaGroupProviderState providerState = createProviderState();
        providerState.setTokens("access-token", "refresh-token");
        return new AccessMeansDTO(USER_ID, providerStateMapper.toJson(providerState), UPDATED_DATE, EXPIRATION_DATE);
    }

    protected BancaTransilvaniaGroupProviderState createProviderState() {
        return new BancaTransilvaniaGroupProviderState(codeExchange, CONSENT_ID, REDIRECT_URI);
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }
}
