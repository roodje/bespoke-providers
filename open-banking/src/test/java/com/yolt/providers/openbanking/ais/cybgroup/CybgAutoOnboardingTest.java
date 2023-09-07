package com.yolt.providers.openbanking.ais.cybgroup;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.cybgroup.common.config.CybgGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupClientRegistration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * This class contains tests for registration logic for data providers for Clydesdale Bank.
 * <p>
 * Covered flows:
 * - registering bank when client-id is not present
 * - ignore registration when client-id is present
 */
@ExtendWith(MockitoExtension.class)
public class CybgAutoOnboardingTest {

    private static final String PROVIDER_KEY = "CLYDESDALE_BANK";
    private static final String REGISTRATION_ENDPOINT = "/v3.2/register";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String CLIENT_ID = "client-id";

    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff679");

    @InjectMocks
    private CybgGroupAutoOnboardingServiceV2 autoOnboarding;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CybgGroupClientRegistration clientRegistration;

    @Mock
    private CybgGroupPropertiesV2 cybgGroupProperties;

    private static final Signer SIGNER = new SignerMock();

    private final CybgGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new CybgGroupSampleAuthenticationMeansV2();

    @Test
    public void shouldInsertOnRegistered() throws IOException, URISyntaxException {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = sampleAuthenticationMeans.getCybgGroupSampleAuthenticationMeansForAis();

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = createAutoOnboardingRequest(authenticationMeans);

        // when
        Optional<CybgGroupClientRegistration> registration = autoOnboarding.register(
                restTemplate,
                urlAutoOnboardingRequest,
                PROVIDER_KEY,
                cybgGroupProperties);

        // then
        assertThat(registration).isEmpty();
    }

    @Test
    public void shouldRegisterCustomer() throws IOException, URISyntaxException {
        // given
        when(restTemplate.exchange(
                eq(REGISTRATION_ENDPOINT),
                eq(HttpMethod.POST),
                any(),
                eq(CybgGroupClientRegistration.class)))
                .thenReturn(ResponseEntity.ok(clientRegistration));

        when(cybgGroupProperties.getRegistrationUrl()).thenReturn(REGISTRATION_ENDPOINT);

        Map<String, BasicAuthenticationMean> authenticationMeans = sampleAuthenticationMeans.getCybgGroupSampleAuthenticationMeansForAis();
        authenticationMeans.remove(CLIENT_ID);

        UrlAutoOnboardingRequest autoOnboardingRequest = createAutoOnboardingRequest(authenticationMeans);

        // when
        Optional<CybgGroupClientRegistration> registration = autoOnboarding.register(
                restTemplate,
                autoOnboardingRequest,
                PROVIDER_KEY,
                cybgGroupProperties);

        // then
        assertThat(registration).contains(clientRegistration);
    }

    private UrlAutoOnboardingRequest createAutoOnboardingRequest(Map<String, BasicAuthenticationMean> authenticationMeans) {
        return new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList(REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();
    }
}
