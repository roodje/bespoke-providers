package com.yolt.providers.dkbgroup.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.dkbgroup.common.auth.AuthenticationService;
import com.yolt.providers.dkbgroup.common.auth.DKBGroupAuthMeans;
import com.yolt.providers.dkbgroup.common.auth.TypedAuthenticationMeansProducer;
import com.yolt.providers.dkbgroup.common.dynamicflow.DynamicFlowHandler;
import com.yolt.providers.dkbgroup.common.http.DKBGroupHttpClientFactory;
import com.yolt.providers.dkbgroup.util.HsmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.ExplanationField;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.PasswordField;
import nl.ing.lovebird.providershared.form.TextField;
import org.apache.commons.lang3.NotImplementedException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.dkbgroup.common.auth.DKBGroupTypedAuthenticationMeansProducer.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.dkbgroup.common.auth.DKBGroupTypedAuthenticationMeansProducer.TRANSPORT_KEY_ID_NAME;

@RequiredArgsConstructor
public class DKBGroupProvider implements UrlDataProvider, AutoOnboardingProvider {

    public static final String PASSWORD_STRING = "password";
    public static final String USERNAME = "username";
    public static final String EXPLANATION = "explanation";
    private static final Duration EMBEDDED_STEP_EXPIRY_DURATION = Duration.ofHours(1);

    @Getter
    private final String providerIdentifier;
    @Getter
    private final String providerIdentifierDisplayName;
    @Getter
    private final ProviderVersion version;

    private final TypedAuthenticationMeansProducer typedAuthenticationMeansProducer;
    private final DKBGroupHttpClientFactory dkbGroupHttpClientFactory;
    private final AuthenticationService authenticationService;
    private final DynamicFlowHandler dynamicFlowHandler;
    private final Clock clock;

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        var username = new TextField(USERNAME, USERNAME, 20, 50, false, false);
        var password = new PasswordField(PASSWORD_STRING, PASSWORD_STRING, 20, 50, false, ".*");
        var explanationField = new ExplanationField(EXPLANATION, EXPLANATION, "Please enter your login and password for DKB.");
        var form = new Form(List.of(username, password), explanationField, null);
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(EMBEDDED_STEP_EXPIRY_DURATION), null);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        DKBGroupAuthMeans authMeans = typedAuthenticationMeansProducer.createAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), providerIdentifier);
        var httpClient = dkbGroupHttpClientFactory.createHttpClient(urlCreateAccessMeans.getRestTemplateManager(), providerIdentifier, authMeans);
        try {
            return dynamicFlowHandler.handle(urlCreateAccessMeans, httpClient);
        } catch (TokenInvalidException exception) {
            throw new GetAccessTokenFailedException(exception);
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) {
        throw new NotImplementedException();
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        throw new NotImplementedException();
    }

    @Override
    public void onUserSiteDelete(final UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        //TODO https://yolt.atlassian.net/browse/C4PO-8878
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        DKBGroupAuthMeans authMeans = typedAuthenticationMeansProducer.createAuthenticationMeans(urlAutoOnboardingRequest.getAuthenticationMeans(), providerIdentifier);
        var httpClient = dkbGroupHttpClientFactory.createHttpClient(urlAutoOnboardingRequest.getRestTemplateManager(), providerIdentifier, authMeans);
        try {
            authenticationService.register(httpClient, providerIdentifierDisplayName);
        } catch (TokenInvalidException exception) {
            throw new AutoOnboardingException(providerIdentifierDisplayName, "Registration request has been rejected.", exception);
        }
        return Collections.emptyMap();
    }
}
