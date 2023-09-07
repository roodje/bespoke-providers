package com.yolt.providers.argentagroup.common.service.consent;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import org.springframework.web.util.UriComponentsBuilder;

public class AuthorizationUrlEnricher {

    private static final String RESPONSE_TYPE_QUERY_PARAMETER = "response_type";
    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String CLIENT_ID_QUERY_PARAMETER = "client_id";
    private static final String SCOPE_QUERY_PARAMETER = "scope";
    private static final String AIS_SCOPE_PREFIX = "AIS:";
    private static final String REDIRECT_URI_QUERY_PARAMETER = "redirect_uri";
    private static final String STATE_QUERY_PARAMETER = "state";
    private static final String CODE_CHALLENGE_QUERY_PARAMETER = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD_QUERY_PARAMETER = "code_challenge_method";


    public String enrichAuthorizationUrl(final InitiateConsentResult initiateConsentResult,
                                         final DefaultAuthenticationMeans authenticationMeans,
                                         final String state,
                                         final String redirectUri,
                                         final OAuth2ProofKeyCodeExchange pkce) {
        return UriComponentsBuilder.fromHttpUrl(initiateConsentResult.getAuthorizationUrl())
                .queryParam(RESPONSE_TYPE_QUERY_PARAMETER, RESPONSE_TYPE_VALUE)
                .queryParam(CLIENT_ID_QUERY_PARAMETER, authenticationMeans.getClientId())
                .queryParam(SCOPE_QUERY_PARAMETER, getAisScope(initiateConsentResult.getConsentId()))
                .queryParam(STATE_QUERY_PARAMETER, state)
                .queryParam(REDIRECT_URI_QUERY_PARAMETER, redirectUri)
                .queryParam(CODE_CHALLENGE_QUERY_PARAMETER, pkce.getCodeChallenge())
                .queryParam(CODE_CHALLENGE_METHOD_QUERY_PARAMETER, pkce.getCodeChallengeMethod())
                .toUriString();
    }

    private String getAisScope(final String consentId) {
        return AIS_SCOPE_PREFIX + consentId;
    }
}
