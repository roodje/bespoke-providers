package com.yolt.providers.openbanking.ais.newdaygroup.autoonboarding;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupApp;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.newdaygroup.amazoncreditcard.AmazonCreditCardDataProviderV3;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.yolt.providers.openbanking.ais.newdaygroup.common.auth.NewDayGroupAuthMeansBuilderV2.CLIENT_ID_NAME;
import static com.yolt.providers.openbanking.ais.newdaygroup.common.auth.NewDayGroupAuthMeansBuilderV2.CLIENT_SECRET_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {NewDayGroupApp.class,
        OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("newdaygroup-v1")
@AutoConfigureWireMock(stubs = "classpath:/stubs/newdaygroup/registration/", httpsPort = 0, port = 0)
public class AutoonboardingIntegrationV2Test {

    private RestTemplateManager restTemplateManagerMock;

    @Autowired
    private AmazonCreditCardDataProviderV3 amazonDataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new NewDayGroupSampleAuthenticationMeansV2().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), any()))
                .thenReturn(new NewDayGroupJwsSigningResult());
    }

    @Test
    public void shouldCorrectlyDoAnAutoOnboarding() {
        // given
        authenticationMeans.remove(CLIENT_ID_NAME);
        authenticationMeans.remove(CLIENT_SECRET_NAME);

        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), any()))
                .thenReturn(new NewDayGroupJwsSigningResult());

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .setRedirectUrls(Collections.singletonList("https://www.yolt.com/callback"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = amazonDataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans.get(CLIENT_SECRET_NAME)).isNotNull();
        assertThat(configureMeans.get(CLIENT_ID_NAME)).isNotNull();
    }
}
