package com.yolt.providers.kbcgroup.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.kbcgroup.common.KbcGroupAuthMeans;
import com.yolt.providers.kbcgroup.common.KbcGroupProperties;
import com.yolt.providers.kbcgroup.common.dto.ConsentUrlData;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupLoginFormDTO;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupTokenResponse;
import com.yolt.providers.kbcgroup.common.rest.KbcGroupHttpClient;
import com.yolt.providers.kbcgroup.common.rest.KbcGroupHttpConstants;
import com.yolt.providers.kbcgroup.common.rest.KbcGroupRestTemplateService;
import com.yolt.providers.kbcgroup.common.util.KbcGroupPKCEUtil;
import com.yolt.providers.kbcgroup.dto.InlineResponse201;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

import static com.yolt.providers.kbcgroup.common.rest.KbcGroupHttpConstants.REDIRECT_URI_QUERY_PARAM_NAME;

@RequiredArgsConstructor
public class KbcGroupAuthenticationService {

    public static final String SCOPE_QUERY_PARAM_NAME = "scope";
    public static final String RESPONSE_TYPE_QUERY_PARAM_NAME = "response_type";
    public static final String CODE_CHALLENGE_QUERY_PARAM_NAME = "code_challenge";
    public static final String CODE_CHALLENGE_METHOD_QUERY_PARAM_NAME = "code_challenge_method";
    public static final String STATE_QUERY_PARAM_NAME = "state";


    private final KbcGroupRestTemplateService restTemplateService;
    private final KbcGroupProperties properties;

    public ConsentUrlData generateLoginUrl(RestTemplateManager restTemplateManager,
                                           KbcGroupAuthMeans authMeans,
                                           KbcGroupLoginFormDTO kbcGroupLoginFormDTO,
                                           String iban,
                                           String psuIpAddress,
                                           String state) {

        KbcGroupHttpClient httpClient = restTemplateService.createHttpClient(authMeans,
                restTemplateManager);

        InlineResponse201 consentResponse = httpClient.createConsent(iban,
                kbcGroupLoginFormDTO.getRedirectUrl(),
                psuIpAddress);

        OAuth2ProofKeyCodeExchange oAuth2ProofKeyCodeExchange = KbcGroupPKCEUtil.createRandomS256();

        String consentUrl = UriComponentsBuilder.fromUriString(properties.getAuthorizationUrl())
                .queryParam(KbcGroupHttpConstants.CLIENT_ID_QUERY_PARAM_NAME, authMeans.getProviderIdentifier())
                .queryParam(REDIRECT_URI_QUERY_PARAM_NAME, kbcGroupLoginFormDTO.getRedirectUrl())
                .queryParam(SCOPE_QUERY_PARAM_NAME, "AIS:" + consentResponse.getConsentId())
                .queryParam(RESPONSE_TYPE_QUERY_PARAM_NAME, "code")
                .queryParam(STATE_QUERY_PARAM_NAME, state)
                .queryParam(CODE_CHALLENGE_QUERY_PARAM_NAME, oAuth2ProofKeyCodeExchange.getCodeChallenge())
                .queryParam(CODE_CHALLENGE_METHOD_QUERY_PARAM_NAME, "S256").toUriString();

        return new ConsentUrlData(consentUrl, oAuth2ProofKeyCodeExchange.getCodeVerifier());
    }

    public KbcGroupTokenResponse obtainAccessToken(UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                   KbcGroupAuthMeans authMeans,
                                                   String authorizationCode,
                                                   String redirectUrl,
                                                   String codeVerifier) {
        KbcGroupHttpClient kbcGroupHttpClient = restTemplateService.createHttpClient(authMeans, urlCreateAccessMeans.getRestTemplateManager());
        return kbcGroupHttpClient.createAccessToken(redirectUrl, authorizationCode, codeVerifier, authMeans.getProviderIdentifier());
    }

    public KbcGroupTokenResponse refreshAccessToken(UrlRefreshAccessMeansRequest urlRefreshAccessMeans,
                                                    KbcGroupAuthMeans authMeans,
                                                    String refreshToken) throws TokenInvalidException {
        KbcGroupHttpClient httpClient = restTemplateService.createHttpClient(authMeans, urlRefreshAccessMeans.getRestTemplateManager());
        return httpClient.refreshAccessToken(refreshToken, authMeans.getProviderIdentifier());
    }
}
