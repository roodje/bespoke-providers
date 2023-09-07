package com.yolt.providers.monorepogroup.atruviagroup;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.monorepogroup.TestSigner;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaFormDecryptor;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.CreateConsentAndObtainScaMethodInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.ObtainChallengeOutcomeInputStep;
import nl.ing.lovebird.providershared.form.ExplanationField;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID_HEADER_STRING;
import static com.yolt.providers.monorepogroup.atruviagroup.EncryptorHelper.*;
import static com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeansFactory.CLIENT_SIGNING_CERTIFICATE_NAME;
import static com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeansFactory.CLIENT_SIGNING_KEY_ID_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AtruviaGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/atruviagroup/ais/happy-flow-no-sca-selection/", httpsPort = 0, port = 0)
@ActiveProfiles("atruviagroup")
class AtruviaGroupDataProviderHappyFlowWithoutSCASelectionIntegrationTest extends AtruviaGroupDataProviderIntegrationTestBase {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STATE = "7c3e98de-0239-4868-ada8-aefb5384ef0a";
    private static final String KEY_ID_VALUE = "11111111-1111-1111-1111-111111111111";
    private static final String REDIRECT_URI = "https://yolt.com/callback";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

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
    void shouldReturnFormStepWithChallengeDataWhereThereIsNothingToSelect() {
        // given
        var incomingProviderState = toAccessMeansString(new CreateConsentAndObtainScaMethodInputStep(SELECTED_REGIONAL_BANK_ID, new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION, PRIVATE_KEY)));
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Map.of("username", encryptSensitiveFieldValueWithJose4j("John", PUBLIC_KEY),
                "password", encryptSensitiveFieldValueWithJose4j("$secret", PUBLIC_KEY)));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setProviderState(incomingProviderState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getStep()).isInstanceOf(FormStep.class);
        var formStep = (FormStep)step.getStep();
        assertThat(formStep.getForm()).isEqualTo(expectedMobileTanChallengeForm());
        var expectedProviderState = extractExpectedProviderState(ObtainChallengeOutcomeInputStep.class, formStep.getProviderState());
        assertThat(expectedProviderState.selectedRegionalBankCode()).isEqualTo(SELECTED_REGIONAL_BANK_ID);
        assertThat(expectedProviderState.consentId()).isEqualTo(CONSENT_ID);
        assertThat(expectedProviderState.authorisationId()).isEqualTo("4593403111270220245PSDGB-FCA-123456AU9545RW");
        assertThat(expectedProviderState.username()).isEqualTo(USERNAME);
        assertThat(expectedProviderState.atruviaEncryptionData().algorithm()).isEqualTo(ALGORITHM);
        assertThat(expectedProviderState.atruviaEncryptionData().encryption()).isEqualTo(ENCRYPTION);
    }

    private Form expectedMobileTanChallengeForm() {
        return new Form(Collections.singletonList(new TextField("challengeData", "mobile TAN", 0, 4096, false, false)), new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", "Die mobileTAN zu diesem Auftrag wird als SMS an Ihre Mobil-Telefonnummer gesendet, die bei Ihrer Bank registriert ist."), null);
    }
}