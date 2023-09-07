package com.yolt.providers.cbiglobe.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.CbiGlobeFixtureProvider;
import com.yolt.providers.cbiglobe.CbiGlobeTestApp;
import com.yolt.providers.cbiglobe.bancawidiba.WidibaDataProviderV2;
import com.yolt.providers.cbiglobe.bcc.BccDataProviderV6;
import com.yolt.providers.cbiglobe.bnl.BnlDataProviderV5;
import com.yolt.providers.cbiglobe.bpm.BpmDataProviderV2;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.cbiglobe.intesasanpaolo.IntesaSanpaoloDataProviderV5;
import com.yolt.providers.cbiglobe.montepaschisiena.MontePaschiSienaDataProviderV5;
import com.yolt.providers.cbiglobe.posteitaliane.PosteItalianeDataProviderV5;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.assertj.core.api.ThrowableAssert;
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
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CbiGlobeTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/ais/3.0/empty_product_codes",
        "classpath:/stubs/ais/3.0/happy_flow/aspspproducts",
        "classpath:/stubs/ais/3.0/happy_flow/accounts"}, httpsPort = 0, port = 0)
@ActiveProfiles("cbiglobe")
public class CbiGlobeDataProviderV5InvalidProductCodeIntegrationTest {

    private static final String STATE_FOR_FIRST_CONSENT_WITH_SCA = "11111111-1111-1111-1111-111111111111";
    private static final String STATE_FOR_FIRST_CONSENT_WITHOUT_SCA = "22222222-2222-2222-2222-222222222222";
    private static final String STATE_FOR_FIRST_CONSENT_WITHOUT_SCA_AND_SMS_OTP = "22222222-2222-3333-3333-222222222222";
    private static final String STATE_FOR_SECOND_CONSENT_WITH_SCA = "33333333-3333-3333-3333-333333333333";

    private static final String FIRST_CONSENT_WITH_SCA_ID = "1";

    private static final String TRANSPORT_KEY_ID = "2be4d475-f240-42c7-a22c-882566ac0f95";
    private static final String SIGNING_KEY_ID = "2e9ecac7-b840-4628-8036-d4998dfb8959";
    private static final String ACCESS_TOKEN = "00000000-0000-0000-0000-000000000000";
    private static final UUID USER_ID = UUID.fromString("7bb49dfa-5d54-43b0-9a39-e4b2b0e9ae10");

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

    @Autowired
    @Qualifier("CbiGlobe")
    private ObjectMapper mapper;

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

    CbiGlobeDataProviderV5[] getProvidersWithSingleASPSP() {
        return new CbiGlobeDataProviderV5[]{
                bnlDataProvider,
                intesaSanpaoloDataProvider,
                montePaschiSienaDataProvider,
                posteItalianeDataProvider,
                bpmDataProvider
        };
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSingleASPSP")
    void shouldLogASPSPProductCodesAndThrowHttpClientErrorExceptionWith400ForGetLoginInfoWithCorrectRequestDataForFirstConsentWithSCA(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlGetLoginRequest urlGetLoginRequest = createUrlGetLoginRequest(STATE_FOR_FIRST_CONSENT_WITH_SCA);

        // when
        ThrowableAssert.ThrowingCallable getLoginInfoCallable = () -> dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThatThrownBy(getLoginInfoCallable)
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 Bad Request: [Body length: 101, Check RDD to see content of body]");

        verify(getRequestedFor(urlEqualTo("/platform/enabler/psd2orchestrator/tpp/aspsps/2.3.2/?aspsp_code=ASPSP_MM_01")));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldLogASPSPProductCodesAndThrowHttpClientErrorExceptionWith400ForCreateNewAccessMeansWithCorrectRequestDataForFirstConsentWithSCA(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeansWithFormValues(
                createCbiGlobeAccessMeansDTO(), STATE_FOR_FIRST_CONSENT_WITH_SCA);

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 Bad Request: [Body length: 101, Check RDD to see content of body]");

        verify(getRequestedFor(urlEqualTo("/platform/enabler/psd2orchestrator/tpp/aspsps/2.3.2/?aspsp_code=ASPSP_MM_01")));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldLogASPSPProductCodesAndThrowHttpClientErrorExceptionWith400ForCreateNewAccessMeansWithCorrectRequestDataForFirstConsentWithoutSCA(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeansWithFormValues(
                createCbiGlobeAccessMeansDTO(), STATE_FOR_FIRST_CONSENT_WITHOUT_SCA);

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 Bad Request: [Body length: 101, Check RDD to see content of body]");

        verify(getRequestedFor(urlEqualTo("/platform/enabler/psd2orchestrator/tpp/aspsps/2.3.2/?aspsp_code=ASPSP_MM_01")));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldLogASPSPProductCodesAndThrowHttpClientErrorExceptionWith400ForCreateNewAccessMeansWithCorrectRequestDataForFirstConsentWithoutSCAAndSmsOtp(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeansWithFormValues(
                createCbiGlobeAccessMeansDTO(), STATE_FOR_FIRST_CONSENT_WITHOUT_SCA_AND_SMS_OTP);

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 Bad Request: [Body length: 101, Check RDD to see content of body]");

        verify(getRequestedFor(urlEqualTo("/platform/enabler/psd2orchestrator/tpp/aspsps/2.3.2/?aspsp_code=ASPSP_MM_01")));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldLogASPSPProductCodesAndThrowHttpClientErrorExceptionWith400ForCreateNewAccessMeansWithCorrectRequestDataForSecondConsentWithSCA(CbiGlobeDataProviderV5 dataProvider) {
        // given
        CbiGlobeAccessMeansDTO providerState = createCbiGlobeAccessMeansDTO(FIRST_CONSENT_WITH_SCA_ID);
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeans(providerState, STATE_FOR_SECOND_CONSENT_WITH_SCA);

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 Bad Request: [Body length: 101, Check RDD to see content of body]");

        verify(getRequestedFor(urlEqualTo("/platform/enabler/psd2orchestrator/tpp/aspsps/2.3.2/?aspsp_code=ASPSP_MM_01")));
    }

    private static String readCertificate() throws IOException, URISyntaxException {
        URL resource = CbiGlobeDataProviderV5HappyFlowIntegrationTest.class
                .getClassLoader().getResource("certificates/fake-certificate.pem");

        Path filePath = new File(Objects.requireNonNull(resource).toURI()).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

    private UrlGetLoginRequest createUrlGetLoginRequest(String state) {
        return new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(state)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .build();
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO() {
        return createCbiGlobeAccessMeansDTO("", ACCESS_TOKEN);
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO(String consentId) {
        return createCbiGlobeAccessMeansDTO(consentId, ACCESS_TOKEN);
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO(String consentId, String accessToken) {
        return createCbiGlobeAccessMeansDTO(consentId, accessToken, Collections.emptyList(), Collections.emptyMap(), 0);
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeans(CbiGlobeAccessMeansDTO accessMeansDTO, String state) {
        return createUrlCreateAccessMeans(accessMeansDTO, state, null);
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansWithFormValues(CbiGlobeAccessMeansDTO accessMeansDTO,
                                                                                 String state) {
        return createUrlCreateAccessMeans(accessMeansDTO, state, getFilledInUserSiteFormValues());
    }

    private FilledInUserSiteFormValues getFilledInUserSiteFormValues() {
        HashMap<String, String> valueMap = new HashMap<>(1);
        valueMap.put("bank", "ASPSP_MM_01");

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(valueMap);
        return filledInUserSiteFormValues;
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeans(CbiGlobeAccessMeansDTO accessMeansDTO,
                                                                   String state,
                                                                   FilledInUserSiteFormValues formValues) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setProviderState(CbiGlobeFixtureProvider.toProviderState(accessMeansDTO, mapper))
                .setSigner(signer)
                .setState(state)
                .setFilledInUserSiteFormValues(formValues)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .build();
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO(String consentId, String accessToken, List<ProviderAccountDTO> accountDTOs, Map<String, ProviderAccountDTO> consentedAccounts, Integer currentlyProcessAccount) {
        return new CbiGlobeAccessMeansDTO(ofEpochMilli(1), accessToken, ofEpochMilli(2), consentId, ofEpochMilli(3), accountDTOs, consentedAccounts, currentlyProcessAccount, "ASPSP_MM_01", null);
    }
}
