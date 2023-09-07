package com.yolt.providers.cbiglobe.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.CbiGlobeTestApp;
import com.yolt.providers.cbiglobe.bancawidiba.WidibaDataProviderV2;
import com.yolt.providers.cbiglobe.bcc.BccDataProviderV6;
import com.yolt.providers.cbiglobe.bnl.BnlDataProviderV5;
import com.yolt.providers.cbiglobe.bpm.BpmDataProviderV2;
import com.yolt.providers.cbiglobe.common.exception.CbiGlobeMalformedObjectException;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.cbiglobe.intesasanpaolo.IntesaSanpaoloDataProviderV5;
import com.yolt.providers.cbiglobe.montepaschisiena.MontePaschiSienaDataProviderV5;
import com.yolt.providers.cbiglobe.posteitaliane.PosteItalianeDataProviderV5;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CbiGlobeTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/ais/3.0/empty_accounts", httpsPort = 0, port = 0)
@ActiveProfiles("cbiglobe")
class CbiGlobeDataProviderV5EmptyAccountsIntegrationTest {

    private static final String STATE_FOR_FIRST_CONSENT_WITH_SCA = "11111111-1111-1111-1111-111111111111";

    private static final String FIRST_CONSENT_WITH_SCA_ID = "1";
    private static final String SECOND_CONSENT_WITH_SCA_ID = "3";

    private static final String ACCESS_TOKEN = "11111111-0000-0000-0000-000000000000";
    private static final String TRANSPORT_KEY_ID = "2be4d475-f240-42c7-a22c-882566ac0f95";
    private static final String SIGNING_KEY_ID = "2e9ecac7-b840-4628-8036-d4998dfb8959";
    private static final UUID USER_ID = UUID.fromString("7bb49dfa-5d54-43b0-9a39-e4b2b0e9ae10");
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Autowired
    @Qualifier("CbiGlobe")
    private ObjectMapper mapper;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private BccDataProviderV6 bccDataProvider;

    @Autowired
    private BnlDataProviderV5 bnlDataProvider;

    @Autowired
    private IntesaSanpaoloDataProviderV5 intesaSanpaoloDataProvider;

    @Autowired
    private MontePaschiSienaDataProviderV5 montePaschiSienaDataProvider;

    @Autowired
    private PosteItalianeDataProviderV5 posteItalianeDataProvider;

    @Autowired
    private WidibaDataProviderV2 widibaDataProvider;

    @Autowired
    private BpmDataProviderV2 bpmDataProvider;

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificate()));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificate()));
        authenticationMeans.put(SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(CLIENT_ID_STRING_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "fakeclientid"));
        authenticationMeans.put(CLIENT_SECRET_STRING_NAME, new BasicAuthenticationMean(TPP_ID.getType(), "fakeclientsecret"));
    }

    CbiGlobeDataProviderV5[] getProviders() {
        return new CbiGlobeDataProviderV5[]{
                bccDataProvider,
                bnlDataProvider,
                intesaSanpaoloDataProvider,
                montePaschiSienaDataProvider,
                posteItalianeDataProvider,
                widibaDataProvider,
                bpmDataProvider
        };
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnNewAccessMeansWithEmptyAccountsForCreateNewAccessMeansWithCorrectRequestDataForSecondConsentWithSCA(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeans();

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThat(accessMeansOrStep.getStep()).isNull();

        AccessMeansDTO accessMeans = accessMeansOrStep.getAccessMeans();
        assertThat(accessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeans.getUpdated()).isEqualTo(Date.from(ofEpochMilli(1)));
        assertThat(accessMeans.getExpireTime()).isEqualTo(Date.from(ofEpochMilli(3)));

        CbiGlobeAccessMeansDTO providerState = fromProviderState(accessMeans.getAccessMeans());
        assertThat(providerState.getConsentId()).isEqualTo(FIRST_CONSENT_WITH_SCA_ID);
        assertThat(providerState.getConsentExpiration()).isEqualTo(providerState.getConsentExpiration());
        assertThat(providerState.getUpdated()).isEqualTo(providerState.getUpdated());
        assertThat(providerState.getAccessTokenExpiration()).isEqualTo(providerState.getAccessTokenExpiration());
        assertThat(providerState.getCachedAccounts()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnResponseWithEmptyAccountsListForFetchDataWithCorrectRequestDataWhenNoAccountAvailable(CbiGlobeDataProviderV5 dataProvider) throws ProviderFetchDataException, TokenInvalidException {
        // given
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchDataRequest);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).isEmpty();
        accounts.forEach(ProviderAccountDTO::validate);
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeans() {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setProviderState(createCbiGlobeAccessMeansDTO(FIRST_CONSENT_WITH_SCA_ID, Collections.emptyList(), Collections.emptyMap(),0))
                .setSigner(signer)
                .setState(STATE_FOR_FIRST_CONSENT_WITH_SCA)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .build();
    }

    private UrlFetchDataRequest createUrlFetchDataRequest() {
        return new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now().minus(Period.ofDays(100)))
                .setAccessMeans(createAccessMeansDTOWithNoCachedAccounts())
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private AccessMeansDTO createAccessMeansDTOWithNoCachedAccounts() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Warsaw"));
        String providerState = createCbiGlobeAccessMeansDTO(SECOND_CONSENT_WITH_SCA_ID, Collections.emptyList(), Collections.emptyMap(),0);
        return new AccessMeansDTO(USER_ID, providerState, Date.from(now.plusDays(7).toInstant()), Date.from(now.plusDays(14).toInstant()));
    }

    private String createCbiGlobeAccessMeansDTO(String consentId, List<ProviderAccountDTO> accountDTOs, Map<String,ProviderAccountDTO> consentedAccount, Integer currentlyProcessAccount) {
        try {
            return mapper.writeValueAsString(new CbiGlobeAccessMeansDTO(ofEpochMilli(1), ACCESS_TOKEN, ofEpochMilli(2), consentId, ofEpochMilli(3), accountDTOs, consentedAccount,currentlyProcessAccount, "ASPSP_MM_01", null));
        } catch (JsonProcessingException e) {
            throw new CbiGlobeMalformedObjectException("Error creating json access means");
        }
    }

    private CbiGlobeAccessMeansDTO fromProviderState(String providerState) {
        try {
            return mapper.readValue(providerState, CbiGlobeAccessMeansDTO.class);
        } catch (IOException e) {
            throw new CbiGlobeMalformedObjectException("Error reading Poste Italiane Access Means");
        }
    }

    private static String readCertificate() throws IOException, URISyntaxException {
        URL resource = CbiGlobeDataProviderV5HappyFlowIntegrationTest.class
                .getClassLoader().getResource("certificates/fake-certificate.pem");

        Path filePath = new File(Objects.requireNonNull(resource).toURI()).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
