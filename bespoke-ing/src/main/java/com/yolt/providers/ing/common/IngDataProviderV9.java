package com.yolt.providers.ing.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.auth.IngUserAccessMeans;
import com.yolt.providers.ing.common.config.HsmUtils;
import com.yolt.providers.ing.common.exception.UnexpectedJsonElementException;
import com.yolt.providers.ing.common.service.IngAuthenticationServiceV3;
import com.yolt.providers.ing.common.service.IngFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.ing.common.auth.IngAuthenticationMeans.*;

@RequiredArgsConstructor
public class IngDataProviderV9 implements UrlDataProvider {

    private final IngFetchDataService ingDataFetch;
    private final IngAuthenticationServiceV3 authenticationService;
    private final ObjectMapper mapper;
    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;
    private final Clock clock;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);

        return typedAuthenticationMeansMap;
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        IngAuthenticationMeans authenticationMeans = createIngAuthenticationMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        IngUserAccessMeans userAccessMeans = jsonToAccessMeans(urlFetchData.getAccessMeans().getAccessMeans(), IngUserAccessMeans.class);
        return ingDataFetch.fetchData(userAccessMeans, authenticationMeans, urlFetchData.getRestTemplateManager(), urlFetchData.getSigner(), urlFetchData.getTransactionsFetchStartTime(), clock);
    }

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        try {
            IngAuthenticationMeans authenticationMeans = createIngAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());
            RestTemplateManager restTemplateManager = urlGetLogin.getRestTemplateManager();
            Signer signer = urlGetLogin.getSigner();
            IngClientAccessMeans clientAccessMeans = authenticationService.getClientAccessMeans(
                    authenticationMeans,
                    urlGetLogin.getAuthenticationMeansReference(),
                    restTemplateManager,
                    signer);
            String ingRedirectUrl = authenticationService.getIngRedirectUrl(
                    clientAccessMeans,
                    authenticationMeans,
                    urlGetLogin.getBaseClientRedirectUrl(),
                    restTemplateManager,
                    signer).getLocation();

            String loginUrl = authenticationService.getLoginUrl(
                    clientAccessMeans.getClientId(),
                    ingRedirectUrl,
                    urlGetLogin.getBaseClientRedirectUrl(),
                    urlGetLogin.getState()
            );
            return new RedirectStep(loginUrl, null, accessMeansToJson(clientAccessMeans));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        try {
            IngAuthenticationMeans authenticationMeans = createIngAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
            IngClientAccessMeans getLoginClientAccessMeans = jsonToAccessMeans(urlCreateAccessMeans.getProviderState(), IngClientAccessMeans.class);
            IngClientAccessMeans clientAccessMeans = authenticationService.getClientAccessMeans(
                    authenticationMeans,
                    getLoginClientAccessMeans.getAuthenticationMeansReference(),
                    urlCreateAccessMeans.getRestTemplateManager(),
                    urlCreateAccessMeans.getSigner());
            IngUserAccessMeans userAccessMeans = authenticationService.getUserToken(
                    clientAccessMeans,
                    authenticationMeans,
                    urlCreateAccessMeans.getRestTemplateManager(),
                    urlCreateAccessMeans.getSigner(),
                    UriComponentsBuilder
                            .fromUriString(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite())
                            .build()
                            .getQueryParams()
                            .toSingleValueMap().get("code"));
            return new AccessMeansOrStepDTO(
                    new AccessMeansDTO(
                            urlCreateAccessMeans.getUserId(),
                            accessMeansToJson(userAccessMeans),
                            new Date(),
                            new Date(userAccessMeans.getExpiryTimestamp())
                    ));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        try {
            IngAuthenticationMeans authenticationMeans = createIngAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
            AccessMeansDTO accessMeans = urlRefreshAccessMeans.getAccessMeans();
            IngUserAccessMeans userAccessMeans = jsonToAccessMeans(accessMeans.getAccessMeans(), IngUserAccessMeans.class);
            RestTemplateManager restTemplateManager = urlRefreshAccessMeans.getRestTemplateManager();
            Signer signer = urlRefreshAccessMeans.getSigner();
            AuthenticationMeansReference authenticationMeansReference = userAccessMeans.getClientAccessMeans().getAuthenticationMeansReference();
            IngClientAccessMeans newIngClientAccessMeans = authenticationService.getClientAccessMeans(authenticationMeans, authenticationMeansReference, restTemplateManager, signer);
            IngUserAccessMeans token = authenticationService.refreshOAuthToken(
                    newIngClientAccessMeans,
                    userAccessMeans,
                    authenticationMeans,
                    restTemplateManager,
                    signer);
            return new AccessMeansDTO(
                    accessMeans.getUserId(),
                    accessMeansToJson(userAccessMeans.update(token, clock)),
                    new Date(),
                    new Date(token.getExpiryTimestamp())
            );
        } catch (GetAccessTokenFailedException e) {
            throw new TokenInvalidException(e.getMessage());
        }
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_KEY_ID, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentifierDisplayName;
    }

    @Override
    public ProviderVersion getVersion() {
        return version;
    }

    private String accessMeansToJson(final Object accessMeans) {
        try {
            return mapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Could not convert access means of " + getProviderIdentifier() + " to JSON");
        }
    }

    private <T> T jsonToAccessMeans(final String json, final Class<T> clazz) throws TokenInvalidException {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to parse " + getProviderIdentifier() + " access means from JSON");
        }
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
