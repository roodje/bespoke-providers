package com.yolt.providers.yoltprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.assertj.core.api.WithAssertions;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.UUID;

import static com.yolt.providers.yoltprovider.TestObjects.createAuthenticationMeans;
import static org.mockito.Mockito.mock;

public class ScenarioTest implements WithAssertions {

    private Scenario scenario;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        scenario = new Scenario(null, null) {
            @Override
            public Step createFirstStep(String stateId, String personaToken, Clock clock) {
                return null;
            }
        };
    }

    @Test
    public void testGetAuthorizationCodeWithFragment() {
        String expected = "testCode";
        UrlCreateAccessMeansRequest request = createRequestForUrl("http://example.com#code=" + expected);
        assertThat(scenario.getAuthorizationCode(request)).isEqualTo(expected);
    }

    @Test
    public void testGetAuthorizationCodeWithParameter() {
        String expected = "testCode";
        UrlCreateAccessMeansRequest request = createRequestForUrl("http://example.com?code=" + expected);
        assertThat(scenario.getAuthorizationCode(request)).isEqualTo(expected);
    }

    @Test
    public void test_embeddedFlow() {
        var restTemplateManagerMock = mock(RestTemplateManager.class);
        var fetchDataServiceMock = mock(YoltProviderFetchDataService.class);
        var yoltProviderAuthorizationServiceMock = mock(YoltProviderAuthorizationService.class);
        YoltProviderConfigurationProperties yoltProviderConfigurationProperties = new YoltProviderConfigurationProperties();
        yoltProviderConfigurationProperties.setCustomerAuthorizationUrl("https://yoltbank/auth");
        YoltProvider yoltProvider = new YoltProvider(yoltProviderConfigurationProperties,  fetchDataServiceMock, yoltProviderAuthorizationServiceMock, Clock.systemUTC());

        Step step1 = yoltProvider.getLoginInfo(new UrlGetLoginRequest("https://baseclientredirect.com", UUID.randomUUID().toString(), null, createAuthenticationMeans(), null, null, restTemplateManagerMock, "127.0.1.13"));

        var selectedBankInput = new FilledInUserSiteFormValues();
        selectedBankInput.add("bank", "branch-1");
        Step usernamePasswordStep = yoltProvider.createNewAccessMeans(new UrlCreateAccessMeansRequest(UUID.randomUUID(), null, null, createAuthenticationMeans(), step1.getProviderState(), null, restTemplateManagerMock,
                selectedBankInput, UUID.randomUUID().toString(), "127.0.1.13")).getStep();

        FormStep usernamePasswordFormStep = (FormStep)usernamePasswordStep;

        FilledInUserSiteFormValues usernamePasswordInput = new FilledInUserSiteFormValues();
        usernamePasswordInput.add("username", encryptValue(usernamePasswordFormStep.getEncryptionDetails().getJweDetails(), "user"));
        usernamePasswordInput.add("password", encryptValue(usernamePasswordFormStep.getEncryptionDetails().getJweDetails(), "password"));

        Step scaMethodSelectionStep = yoltProvider.createNewAccessMeans(new UrlCreateAccessMeansRequest(UUID.randomUUID(), null, null, createAuthenticationMeans(), usernamePasswordFormStep.getProviderState(), null, restTemplateManagerMock,
                usernamePasswordInput, UUID.randomUUID().toString(), "127.0.1.13")).getStep();

        FilledInUserSiteFormValues scaMethodInput = new FilledInUserSiteFormValues();
        scaMethodInput.add("scaMethod", "SMS_OTP");
        Step challengeStep = yoltProvider.createNewAccessMeans(new UrlCreateAccessMeansRequest(UUID.randomUUID(), null, null, createAuthenticationMeans(), scaMethodSelectionStep.getProviderState(), null, restTemplateManagerMock,
                scaMethodInput, UUID.randomUUID().toString(), "127.0.1.13")).getStep();
        FormStep challengeFormStep = (FormStep)challengeStep;

        FilledInUserSiteFormValues challengeInput = new FilledInUserSiteFormValues();
        challengeInput.add("challengeData", encryptValue(challengeFormStep.getEncryptionDetails().getJweDetails(), "123"));

        AccessMeansOrStepDTO accessMeansOrStepDTO = yoltProvider.createNewAccessMeans(new UrlCreateAccessMeansRequest(UUID.randomUUID(), null, null, createAuthenticationMeans(), challengeStep.getProviderState(), null, restTemplateManagerMock,
                challengeInput, UUID.randomUUID().toString(), "127.0.1.13"));
        assertThat(accessMeansOrStepDTO.getAccessMeans()).isNotNull();

    }

    @SneakyThrows
    String encryptValue(EncryptionDetails.JWEDetails jweDetails, String payload) {
        String jsonJwk = objectMapper.writeValueAsString(jweDetails.getRsaPublicJwk());
        PublicJsonWebKey publicJsonWebKey = (PublicJsonWebKey) JsonWebKey.Factory.newJwk(jsonJwk);
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.setAlgorithmHeaderValue(jweDetails.getRsaPublicJwk().getAlg());
        jwe.setEncryptionMethodHeaderParameter(jweDetails.getEncryptionMethod());
        jwe.setJwkHeader(publicJsonWebKey);
        jwe.setKey(publicJsonWebKey.getPublicKey());
        jwe.setPlaintext(payload);
        return jwe.getCompactSerialization();
    }

    private UrlCreateAccessMeansRequest createRequestForUrl(String url) {
        return new UrlCreateAccessMeansRequest(null,
                url,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
