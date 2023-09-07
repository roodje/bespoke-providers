package com.yolt.providers.sparkassenandlandesbanks.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.sparkassenandlandesbanks.common.Department;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAccessMeans;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAuthMeans;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.*;
import com.yolt.providers.sparkassenandlandesbanks.common.rest.SparkassenAndLandesbanksHttpClient;
import com.yolt.providers.sparkassenandlandesbanks.common.rest.SparkassenAndLandesbanksRestTemplateService;
import com.yolt.providers.sparkassenandlandesbanks.common.util.SparkassenAndLandesbanksPKCEUtil;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static net.logstash.logback.marker.Markers.append;

@RequiredArgsConstructor
@Slf4j
public class SparkassenAndLandesbanksAuthenticationService {

    public static final String SCOPE_QUERY_PARAM_NAME = "scope";
    public static final String CODE_CHALLENGE_QUERY_PARAM_NAME = "code_challenge";
    public static final String CODE_CHALLENGE_METHOD_QUERY_PARAM_NAME = "code_challenge_method";
    public static final String STATE_QUERY_PARAM_NAME = "state";

    private static final String CLIENT_ID_QUERY_PARAM_NAME = "clientId";
    private static final String RESPONSE_TYPE_QUERY_PARAM_NAME = "responseType";
    private static final Marker RDD_MARKER = append("raw-data", "true");

    private final SparkassenAndLandesbanksRestTemplateService restTemplateService;

    public ConsentUrlData generateLoginUrl(SparkassenAndLandesbanksAuthMeans authMeans,
                                           Department department,
                                           String provider,
                                           RestTemplateManager restTemplateManager,
                                           String baseClientRedirectUrl,
                                           String psuIpAddress,
                                           String state) throws TokenInvalidException {

        SparkassenAndLandesbanksHttpClient httpClient = restTemplateService.createHttpClient(authMeans,
                restTemplateManager,
                provider);

        ConsentResponse consentResponse = httpClient.createConsent(department,
                baseClientRedirectUrl,
                psuIpAddress);

        OAuth2ProofKeyCodeExchange oAuth2ProofKeyCodeExchange = SparkassenAndLandesbanksPKCEUtil.createRandomS256();

        SparkassenAndLandesbanksHttpClient sparkassenAndLandesbanksHttpClient = restTemplateService.createHttpClient(authMeans, restTemplateManager, provider);
        String wellKnownEndpoint = consentResponse.getLinks().getScaOAuth().getHref();
        OAuthLinksResponse oAuthLinksResponse = sparkassenAndLandesbanksHttpClient.getDepartmentOAuthLinks(wellKnownEndpoint);

        String consentUrl = UriComponentsBuilder.fromHttpUrl(oAuthLinksResponse.getAuthorizationEndpoint())
                .queryParam(CLIENT_ID_QUERY_PARAM_NAME, authMeans.getClientId())
                .queryParam(SCOPE_QUERY_PARAM_NAME, "AIS%3A%20" + consentResponse.getConsentId())
                .queryParam(RESPONSE_TYPE_QUERY_PARAM_NAME, "code")
                .queryParam(STATE_QUERY_PARAM_NAME, state)
                .queryParam(CODE_CHALLENGE_QUERY_PARAM_NAME, oAuth2ProofKeyCodeExchange.getCodeChallenge())
                .queryParam(CODE_CHALLENGE_METHOD_QUERY_PARAM_NAME, "S256")
                .build()
                .toString();

        log.debug(RDD_MARKER, "Consent page URL: {}", consentUrl);

        return new ConsentUrlData(consentUrl, consentResponse.getConsentId(), oAuth2ProofKeyCodeExchange.getCodeVerifier(), wellKnownEndpoint);
    }

    public SparkassenAndLandesbanksTokenResponse obtainAccessToken(UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                                   SparkassenAndLandesbanksAuthMeans authMeans,
                                                                   SparkassenAndLandesbanksProviderState sparkassenAndLandesbanksProviderState,
                                                                   String provider) throws TokenInvalidException {

        String redirectUrl = urlCreateAccessMeans.getRedirectUrlPostedBackFromSite();
        String authorizationCode = retrieveAuthCodeFromRedirectUrl(redirectUrl);

        redirectUrl = redirectUrl.substring(0, redirectUrl.indexOf('?'));

        SparkassenAndLandesbanksHttpClient sparkassenAndLandesbanksHttpClient = restTemplateService.createHttpClient(authMeans, urlCreateAccessMeans.getRestTemplateManager(), provider);

        String tokenEndpoint = sparkassenAndLandesbanksHttpClient
                .getDepartmentOAuthLinks(sparkassenAndLandesbanksProviderState.getWellKnownEndpoint())
                .getTokenEndpoint();

        return sparkassenAndLandesbanksHttpClient.createAccessToken(redirectUrl,
                authorizationCode,
                sparkassenAndLandesbanksProviderState.getCodeVerifier(),
                authMeans.getClientId(),
                tokenEndpoint);
    }

    public SparkassenAndLandesbanksTokenResponse refreshAccessToken(UrlRefreshAccessMeansRequest urlRefreshAccessMeans,
                                                                    SparkassenAndLandesbanksAuthMeans authMeans,
                                                                    SparkassenAndLandesbanksAccessMeans accessMeans,
                                                                    String provider) throws TokenInvalidException {
        SparkassenAndLandesbanksHttpClient httpClient = restTemplateService.createHttpClient(authMeans, urlRefreshAccessMeans.getRestTemplateManager(), provider);
        String tokenEndpoint = httpClient.getDepartmentOAuthLinks(accessMeans.getWellKnownEndpoint()).getTokenEndpoint();
        return httpClient.refreshAccessToken(accessMeans.getRefreshToken(), authMeans.getClientId(), tokenEndpoint);
    }

    protected String retrieveAuthCodeFromRedirectUrl(String redirectUrl) {
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        validateIfNoError(queryParams, redirectUrl);

        String authorizationCode = queryParams.get("code");
        if (!StringUtils.hasLength(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }
        return authorizationCode;
    }

    private void validateIfNoError(Map<String, String> queryParams, String redirectUrl) {
        final String error = queryParams.get("error");
        if (StringUtils.hasLength(error)) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrl);
        }
    }
}
