package com.yolt.providers.nutmeggroup.nutmeg;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.nutmeggroup.common.AuthenticationMeansV2;
import com.yolt.providers.nutmeggroup.common.OAuthConstants;
import com.yolt.providers.nutmeggroup.common.service.NutmegGroupAuthorizationServiceV2;
import com.yolt.providers.nutmeggroup.common.service.NutmegGroupFetchDataServiceV2;
import com.yolt.providers.nutmeggroup.nutmeg.configuration.NutmegProperties;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_2;
import static com.yolt.providers.nutmeggroup.common.AuthenticationMeansV2.CLIENT_ID;
import static com.yolt.providers.nutmeggroup.common.utils.NutmegPKCE.createRandomS256;

@Service
@RequiredArgsConstructor
public class NutmegDataProviderV2 implements UrlDataProvider {

    private final NutmegGroupAuthorizationServiceV2 authorizationService;
    private final NutmegGroupFetchDataServiceV2 fetchDataService;
    private final NutmegProperties properties;

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        OAuth2ProofKeyCodeExchange pkcsCodes = createRandomS256();
        AuthenticationMeansV2 authenticationMeans = AuthenticationMeansV2.getAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifierDisplayName());

        String redirectUrl = UriComponentsBuilder.fromHttpUrl(properties.getAuthorizeUrl())
                .queryParam(OAuthConstants.Params.RESPONSE_TYPE, OAuthConstants.Values.CODE)
                .queryParam(OAuthConstants.Params.CODE_CHALLENGE, pkcsCodes.getCodeChallenge())
                .queryParam(OAuthConstants.Params.CODE_CHALLENGE_METHOD, properties.getCodeChallengeMethod())
                .queryParam(OAuthConstants.Params.CLIENT_ID, authenticationMeans.getClientId())
                .queryParam(OAuthConstants.Params.SCOPE, properties.getScope())
                .queryParam(OAuthConstants.Params.REDIRECT_URI, urlGetLogin.getBaseClientRedirectUrl())
                .queryParam(OAuthConstants.Params.AUDIENCE, properties.getAudience())
                .queryParam(OAuthConstants.Params.STATE, urlGetLogin.getState())
                .build()
                .encode()
                .toString();

        return new RedirectStep(redirectUrl, null, pkcsCodes.getCodeVerifier());
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return authorizationService.createAccessMeans(urlCreateAccessMeans, getProviderIdentifierDisplayName());
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        return authorizationService.refreshAccessMeans(urlRefreshAccessMeans, getProviderIdentifierDisplayName());
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);

        return typedAuthenticationMeansMap;
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        return fetchDataService.fetchdata(urlFetchData);
    }

    @Override
    public String getProviderIdentifier() {
        return "NUTMEG";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Nutmeg";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_2;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        Set<String> keywords = new HashSet<>();
        keywords.add("Sign in to Nutmeg");
        keywords.add("Log In");
        keywords.add("Don’t have an account?");
        keywords.add("Sign up");
        return new ConsentValidityRules(keywords);
    }
}