package com.yolt.providers.cbiglobe.bpm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.CbiGlobeFixtureProvider;
import com.yolt.providers.cbiglobe.CbiGlobeTestApp;
import com.yolt.providers.cbiglobe.common.CbiGlobeDataProviderV5;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
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
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CbiGlobeTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/ais/3.0/happy_flow/accounts/bpm", httpsPort = 0, port = 0)
@ActiveProfiles("cbiglobe")
class BpmDataProviderV2HappyFlowIntegrationTest {

    private static final String CONSENT_ID_WITH_SCA = "3";
    private static final String ACCOUNT_ID = "5";
    private static final String TRANSPORT_KEY_ID = "2be4d475-f240-42c7-a22c-882566ac0f95";
    private static final String SIGNING_KEY_ID = "2e9ecac7-b840-4628-8036-d4998dfb8959";
    private static final String ACCESS_TOKEN = "00000000-0000-0000-0000-000000000000";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Autowired
    @Qualifier("CbiGlobe")
    private ObjectMapper mapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private BpmDataProviderV2 bpmDataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

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

    Stream<CbiGlobeDataProviderV5> getProviders() {
        return Stream.of(bpmDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnCorrectDataWhenFetchingDataWithCorrectRequestData(CbiGlobeDataProviderV5 dataProvider) throws ProviderFetchDataException, TokenInvalidException {
        // given
        ProviderAccountDTO providerAccountDTO = CbiGlobeFixtureProvider.createProviderAccountDTO(ACCOUNT_ID);
        CbiGlobeAccessMeansDTO givenAccessMeans = createCbiGlobeAccessMeansDTO(CONSENT_ID_WITH_SCA, ACCESS_TOKEN, Collections.singletonList(providerAccountDTO));
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(givenAccessMeans);

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchDataRequest);

        // then
        // Validate accounts
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(1);
        accounts.forEach(ProviderAccountDTO::validate);

        // Validate account 1
        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account1.getAvailableBalance()).isEqualTo("33");
        assertThat(account1.getCurrentBalance()).isEqualTo("22");
        assertThat(account1.getTransactions()).isEmpty();
        account1.validate();
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(CbiGlobeAccessMeansDTO accessMeansDTO) {
        return new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now().minus(Period.ofDays(100)))
                .setAccessMeans(CbiGlobeFixtureProvider.createAccessMeansDTO(accessMeansDTO, mapper))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO(String consentId, String accessToken, List<ProviderAccountDTO> accountDTOs) {
        Map<String,ProviderAccountDTO> consentedAccount = new HashMap<>();
        accountDTOs.stream().forEach(account -> consentedAccount.put(consentId,account));
        return new CbiGlobeAccessMeansDTO(ofEpochMilli(1), accessToken, ofEpochMilli(2), consentId, ofEpochMilli(3), accountDTOs, consentedAccount,1, "ASPSP_MM_01", null);
    }

    private static String readCertificate() throws IOException, URISyntaxException {
        URL resource = BpmDataProviderV2HappyFlowIntegrationTest.class
                .getClassLoader().getResource("certificates/fake-certificate.pem");

        Path filePath = new File(Objects.requireNonNull(resource).toURI()).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}