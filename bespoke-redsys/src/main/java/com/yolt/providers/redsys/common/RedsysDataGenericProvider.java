package com.yolt.providers.redsys.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import com.yolt.providers.redsys.common.dto.ResponseGetConsent;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.common.service.RedsysAuthorizationService;
import com.yolt.providers.redsys.common.service.RedsysFetchDataServiceV2;
import com.yolt.providers.redsys.common.util.HsmUtils;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans.*;

public abstract class RedsysDataGenericProvider implements UrlDataProvider {

    private final RedsysBaseProperties properties;
    private final RedsysAuthorizationService authorizationService;
    private final RedsysFetchDataServiceV2 fetchDataService;
    private final ObjectMapper mapper;
    private final Clock clock;

    private static final String AUTHORIZE_ENDPOINT = "/authorize";

    public RedsysDataGenericProvider(final RedsysBaseProperties properties,
                                     final RedsysAuthorizationService authorizationService,
                                     final RedsysFetchDataServiceV2 fetchDataService,
                                     final ObjectMapper mapper,
                                     final Clock clock) {
        this.properties = properties;
        this.authorizationService = authorizationService;
        this.fetchDataService = fetchDataService;
        this.mapper = mapper;
        this.clock = clock;
    }

    protected AccessMeansOrStepDTO createActualAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                           final Boolean shouldFlowAlsoReturnGetScaRedirectLink) {
        if (Objects.isNull(urlCreateAccessMeans)) {
            throw new MissingDataException("Missing UrlCreateAccessMeansRequest");
        }

        if (StringUtils.isEmpty(urlCreateAccessMeans.getProviderState())) {
            throw new MissingDataException("Missing provider state");
        }

        String providerState = urlCreateAccessMeans.getProviderState();
        RedsysAccessMeans providerStateAccessMeans;
        try {
            providerStateAccessMeans = deserializeAccessMeans(providerState);

        } catch (TokenInvalidException e) {
            throw new MissingDataException("Missing provider state");
        }

        RedsysAuthenticationMeans authenticationMeans = RedsysAuthenticationMeans.fromAuthenticationMeans(
                urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        String redirectUrl = getRedirectUrl(urlCreateAccessMeans);
        String redirectUrlWithState = createCallbackUrlWithState(redirectUrl, urlCreateAccessMeans.getState());

        if (StringUtils.isBlank(providerStateAccessMeans.getConsentId())) {

            Token accessToken = authorizationService.createAccessToken(urlCreateAccessMeans,
                    redirectUrl, authenticationMeans, providerStateAccessMeans.getCodeVerifier());

            ResponseGetConsent consent = authorizationService.getConsentId(urlCreateAccessMeans,
                    authenticationMeans,
                    accessToken.getAccessToken(),
                    getConsentValidUntilDate(clock),
                    redirectUrlWithState,
                    urlCreateAccessMeans.getFilledInUserSiteFormValues());

            providerState = serializeAccessMeans(
                    new RedsysAccessMeans(accessToken,
                            redirectUrl,
                            consent.getConsentId(),
                            providerStateAccessMeans.getCodeVerifier(),
                            Instant.now(clock),
                            urlCreateAccessMeans.getFilledInUserSiteFormValues())
            );

            if (shouldFlowAlsoReturnGetScaRedirectLink) {
                return new AccessMeansOrStepDTO(
                        new RedirectStep(getScaRedirectLink(consent), null, providerState));
            } else {
                providerStateAccessMeans.setToken(accessToken);
            }
        }

        if (Objects.isNull(providerStateAccessMeans.getToken())) {
            throw new MissingDataException("Could not extract token from provider state");
        }
        // providerState does not change
        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        providerState,
                        new Date(),
                        // caveat - here the consent starts to live, not the access token
                        Date.from(Instant.now(clock).plusSeconds(providerStateAccessMeans.getToken().getExpiresIn()))
                )
        );
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        RedsysAuthenticationMeans authenticationMeans = RedsysAuthenticationMeans.fromAuthenticationMeans(
                urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        AccessMeansDTO accessMeansDTO = urlRefreshAccessMeans.getAccessMeans();
        RedsysAccessMeans redsysAccessMeans = deserializeAccessMeans(accessMeansDTO.getAccessMeans());

        Token accessToken = authorizationService.createNewAccessTokenFromRefreshToken(
                urlRefreshAccessMeans, redsysAccessMeans, authenticationMeans);

        if (accessToken.getRefreshToken() == null) {
            throw new TokenInvalidException();
        }

        String accessMeans = serializeAccessMeans(new RedsysAccessMeans(accessToken, redsysAccessMeans.getRedirectUrl(), redsysAccessMeans.getConsentId(), null, redsysAccessMeans.getConsentAt(), redsysAccessMeans.getFormValues()));

        return new AccessMeansDTO(
                accessMeansDTO.getUserId(),
                accessMeans,
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(accessToken.getExpiresIn()))
        );
    }

    protected String createAuthorizationUrl(final String clientId,
                                            final String redirectUrl,
                                            final String state,
                                            final OAuth2ProofKeyCodeExchange oAuth2ProofKeyCodeExchange) {

        MultiValueMap<String, String> varMap = new LinkedMultiValueMap<>();
        varMap.add("response_type", "code");
        varMap.add("scope", "AIS");
        varMap.add("state", state);
        varMap.add("redirect_uri", redirectUrl);
        varMap.add("client_id", clientId);
        varMap.add("code_challenge", oAuth2ProofKeyCodeExchange.getCodeChallenge());
        varMap.add("code_challenge_method", oAuth2ProofKeyCodeExchange.getCodeChallengeMethod());

        return UriComponentsBuilder.fromHttpUrl(
                properties.getAuthorizationUrl() + AUTHORIZE_ENDPOINT).queryParams(varMap).build().encode().toString();
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {

        RedsysAuthenticationMeans authenticationMeans = RedsysAuthenticationMeans.fromAuthenticationMeans(
                urlFetchData.getAuthenticationMeans(), getProviderIdentifier());

        RedsysAccessMeans accessMeansDTO = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());

        return fetchDataService.fetchData(authenticationMeans, accessMeansDTO, getProviderIdentifierDisplayName(), urlFetchData);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private static String getRedirectUrl(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        String redirectUrl = urlCreateAccessMeans.getRedirectUrlPostedBackFromSite();
        int queryParamStartIndex = redirectUrl.indexOf('?');
        return redirectUrl.substring(0, queryParamStartIndex);
    }

    protected String serializeAccessMeans(final RedsysAccessMeans redsysAccessMeans) {
        try {
            return mapper.writeValueAsString(redsysAccessMeans);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize access means.");
        }
    }

    RedsysAccessMeans deserializeAccessMeans(final String accessMean) throws TokenInvalidException {
        RedsysAccessMeans redsysAccessMeans;
        try {
            redsysAccessMeans = mapper.readValue(accessMean, RedsysAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means.");
        }
        return redsysAccessMeans;
    }

    static LocalDate getConsentValidUntilDate(final Clock clock) {
        return LocalDate.now(clock).plusDays(90);
    }

    String getScaRedirectLink(ResponseGetConsent consent) {
        if (consent == null || consent.getLinks() == null || consent.getLinks().getScaRedirect() == null || consent.getLinks().getScaRedirect().getHref() == null) {
            throw new IllegalStateException("Consent response or SCA redirect link is empty.");
        }
        return consent.getLinks().getScaRedirect().getHref();
    }

    String createCallbackUrlWithState(String callbackUrl, String state) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .queryParam("state", state)
                .toUriString();
    }
}
