package com.yolt.providers.yoltprovider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.PemType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.domain.authenticationmeans.types.UuidType;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.SelectField;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/ais", files = "classpath:/stubs/ais", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestApp.class})
public class YoltProviderIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String AUTHORIZATION_CODE_200 = "5f9c4ab08cac7457e9111a30e4664920607ea2c115a1433d7be98e97e64244ca";
    private static final String AUTHORIZATION_CODE_403 = "5f9c4ab08cac7457e9111a30e4664920607ea2c115a1433d7be98e97e64244cb";
    private static final String SECRET_STATE = "secretState";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback-dev";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String ACCESS_TOKEN_BAD = "badToken";

    private final RestTemplateManagerMock restTemplateManager = new RestTemplateManagerMock();
    private final Signer signer = mock(Signer.class);
    private final ObjectMapper objectMapper = new YoltBankBeanConfig().yoltBankObjectMapper();

    @Autowired
    @Qualifier("YoltProviderVersion1")
    private YoltProvider yoltProvider;

    @Autowired
    private YoltProviderConfigurationProperties configurationProperties;

    @Test
    public void shouldReturnRedirectStepWithRedirectUrlForGetLoginInfoWhenOnly1RedirectStep() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(new URI(loginInfo.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", SECRET_STATE);
    }

    @Test
    public void shouldReturnNewAccessMeansWithTheSameAuthCodeForCreateNewAccessMeansWithCorrectRequestData() throws Exception {
        // given
        RedirectFirstScenario formFirstScenario = new RedirectFirstScenario(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        String providerState = objectMapper.writeValueAsString(formFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + AUTHORIZATION_CODE_200)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(providerState)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        AccessMeansDTO newAccessMeans = yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getAccessMeans();

        // then
        assertThat(newAccessMeans.getAccessMeans()).isEqualTo(AUTHORIZATION_CODE_200);
    }

    @Test
    public void shouldReturnFormStepWithSingleSelectFieldComponentForGetLoginInfoWhenBeforeRedirectFlow() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.0.2")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(1);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(SelectField.class);
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
    }

    @Test
    public void shouldReturnRedirectStepWithSelectedRegionForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided() throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("region", "region-a");
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(FormFirstScenario.regionSelection(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("region", "region-a");
    }

    @Test
    public void shouldReturnFormStepWithRegionSelectionFormComponentsForCreateNewAccessMeansWhenTriggeringDynamicFlow() throws Exception {
        // given
        FormFirstScenario formFirstScenario = FormFirstScenario.regionSelection(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        String providerState = objectMapper.writeValueAsString(formFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setProviderState(providerState)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + AUTHORIZATION_CODE_200 + "&trigger-dynamic-flow=elaborate-form")
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        FormStep formStep = (FormStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(formStep.getForm().getFormComponents()).hasSize(7);
    }

    @Test
    public void shouldReturnNewAccessMeansForCreateNewAccessMeansWhenAllRequiredStepsFulfilled() throws Exception {
        // given
        RedirectFirstScenario formFirstScenario = new RedirectFirstScenario(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        formFirstScenario.setAuthorizationCode(AUTHORIZATION_CODE_200);
        String providerState = objectMapper.writeValueAsString(formFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeansSubmitForm = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(new FilledInUserSiteFormValues())
                .setProviderState(providerState)
                .setState(UUID.randomUUID().toString())
                .setUserId(USER_ID)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        AccessMeansDTO newAccessMeans = yoltProvider
                .createNewAccessMeans(urlCreateAccessMeansSubmitForm)
                .getAccessMeans();

        // then
        assertThat(newAccessMeans.getAccessMeans()).isEqualTo(AUTHORIZATION_CODE_200);
    }

    @Test
    public void shouldReturnDataForFetchDataWithCorrectRequestData() throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(createAccessMeansDTO(false))
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        DataProviderResponse dataProviderResponse = yoltProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);
    }

    @Test
    public void shouldThrowHttpClientErrorForbiddenExceptionForCreateNewAccessMeansWhenForbiddenErrorInResponse() throws Exception {
        // given
        RedirectFirstScenario formFirstScenario = new RedirectFirstScenario(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        String providerState = objectMapper.writeValueAsString(formFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + AUTHORIZATION_CODE_403)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(providerState)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> yoltProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionForDataFetchWithForbiddenResponse() throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(createAccessMeansDTO(true))
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        Executable executable = () -> yoltProvider.fetchData(urlFetchData);

        // then
        assertThrows(TokenInvalidException.class, executable);
    }

    @Test
    void shouldCreateExtraConsentScenarioIfRedirectUriHasParamSet() throws Exception {
        // given
        RedirectFirstScenario redirectFirstScenario = new RedirectFirstScenario(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        String providerState = objectMapper.writeValueAsString(redirectFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + AUTHORIZATION_CODE_200 + "&consent-redirect=yes")
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(providerState)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        AccessMeansOrStepDTO newAccessMeans = yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans);

        // then
        Scenario scenario = objectMapper.readValue(newAccessMeans.getStep().getProviderState(), Scenario.class);
        assertThat(scenario)
                .isExactlyInstanceOf(ExtraConsentScenario.class)
                .extracting(Scenario::getAccessMeans)
                .returns(AUTHORIZATION_CODE_200, AccessMeansDTO::getAccessMeans);
    }

    @Test
    void shouldDeserializeExtraConsentStepAndReturnAccessMeans() throws Exception {
        // given
        ExtraConsentScenario scenario = new ExtraConsentScenario(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        scenario.setAccessMeans(createAccessMeansDTO(false));
        String providerState = objectMapper.writeValueAsString(scenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeansSubmitForm = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(new FilledInUserSiteFormValues())
                .setProviderState(providerState)
                .setState(UUID.randomUUID().toString())
                .setUserId(USER_ID)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        AccessMeansDTO newAccessMeans = yoltProvider
                .createNewAccessMeans(urlCreateAccessMeansSubmitForm)
                .getAccessMeans();

        // then
        assertThat(newAccessMeans.getAccessMeans()).isEqualTo(AUTHORIZATION_CODE_200);
    }

    @Test
    public void shouldReturnEncryptedFormStepAndDecryptValues() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.12")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep formStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(formStep.getEncryptionDetails()).isNotNull();
        var jweDetails = formStep.getEncryptionDetails().getJweDetails();
        assertThat(jweDetails).isNotNull();
        assertThat(jweDetails.getEncryptionMethod()).isEqualTo("A256GCM");
        var jwk = jweDetails.getRsaPublicJwk();
        assertThat(jwk).isNotNull();
        assertThat(jwk.getAlg()).isEqualTo("RSA-OAEP-256");
        assertThat(jwk.getKty()).isEqualTo("RSA");
        assertThat(jwk.getN()).isNotNull();
        assertThat(jwk.getE()).isNotNull();

        String jsonJwk = objectMapper.writeValueAsString(jwk);

        PublicJsonWebKey publicJsonWebKey = (PublicJsonWebKey) JsonWebKey.Factory.newJwk(jsonJwk);
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setAlgorithmHeaderValue(jwk.getAlg());
        jwe.setEncryptionMethodHeaderParameter(jweDetails.getEncryptionMethod());
        jwe.setJwkHeader(publicJsonWebKey);
        jwe.setKey(publicJsonWebKey.getPublicKey());
        jwe.setPlaintext("John");
        var codedUsername = jwe.getCompactSerialization();
        jwe.setPlaintext("JohnJohn");
        var codedPass = jwe.getCompactSerialization();

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("username", codedUsername);
        filledInUserSiteFormValues.add("password", codedPass);

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + AUTHORIZATION_CODE_200)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(formStep.getProviderState())
                .setAuthenticationMeans(createAuthenticationMeans())
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .build();

        // when
        var step = yoltProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(step.getStep()).isInstanceOf(RedirectStep.class);
        var redirectStep = (RedirectStep) step.getStep();
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL);
    }

    private AccessMeansDTO createAccessMeansDTO(boolean badToken) {
        return new AccessMeansDTO(USER_ID, badToken ? ACCESS_TOKEN_BAD : ACCESS_TOKEN, new Date(), new Date());
    }

    private Map<String, BasicAuthenticationMean> createAuthenticationMeans() throws Exception {
        URI fileURI = YoltProviderIntegrationTest.class
                .getClassLoader()
                .getResource("test_certificate.pem")
                .toURI();
        String certificate = String.join("\n", Files.readAllLines(new File(fileURI).toPath(), StandardCharsets.UTF_8));
        return new HashMap<String, BasicAuthenticationMean>() {{
            put("client-id", new BasicAuthenticationMean(StringType.getInstance(), "someId"));
            put("client-secret", new BasicAuthenticationMean(StringType.getInstance(), "someId"));
            put("client-signing-private-key-id", new BasicAuthenticationMean(UuidType.getInstance(), UUID.randomUUID().toString()));
            put("client-transport-private-key-id", new BasicAuthenticationMean(UuidType.getInstance(), UUID.randomUUID().toString()));
            put("client-signing-certificate", new BasicAuthenticationMean(PemType.getInstance(), certificate));
            put("client-transport-certificate", new BasicAuthenticationMean(PemType.getInstance(), certificate));
        }};
    }
}
