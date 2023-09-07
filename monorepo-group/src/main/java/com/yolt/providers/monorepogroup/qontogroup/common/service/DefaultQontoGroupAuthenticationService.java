package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;
import com.yolt.providers.monorepogroup.qontogroup.common.http.QontoGroupHttpClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class DefaultQontoGroupAuthenticationService implements QontoGroupAuthenticationService {

    private static final String SCOPE_VALUE = "organization.read offline_access";
    private static final String ERROR = "error";
    private final String authorizationUrl;
    private final Clock clock;

    @Override
    public String getLoginUrl(final QontoGroupAuthenticationMeans authenticationMeans, final String baseRedirectUrl, final String state) {
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString(authorizationUrl)
                .queryParam(OAuth.CLIENT_ID, authenticationMeans.getClientId())
                .queryParam(OAuth.REDIRECT_URI, baseRedirectUrl)
                .queryParam(OAuth.RESPONSE_TYPE, OAuth.CODE)
                .queryParam(OAuth.STATE, state)
                .queryParam(OAuth.SCOPE, SCOPE_VALUE);
        return uriComponentsBuilder.toUriString();
    }

    @Override
    public QontoGroupProviderState createAccessMeans(QontoGroupAuthenticationMeans authenticationMeans, QontoGroupHttpClient httpClient, String baseClientRedirectUrl, String redirectUrlPostedBackFromSite) throws TokenInvalidException {
        verifyRedirectUrl(redirectUrlPostedBackFromSite);
        var code = getCode(redirectUrlPostedBackFromSite);
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add(CODE, code);
        tokenRequest.add(CLIENT_ID, authenticationMeans.getClientId());
        tokenRequest.add(CLIENT_SECRET, authenticationMeans.getClientSecret());
        tokenRequest.add(REDIRECT_URI, baseClientRedirectUrl);
        tokenRequest.add(GRANT_TYPE, AUTHORIZATION_CODE);
        var tokenResponse = httpClient.createToken(tokenRequest);
        return new QontoGroupProviderState(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), Instant.now(clock).plusSeconds(tokenResponse.getExpiresInSeconds()).toEpochMilli());
    }

    @Override
    public QontoGroupProviderState refreshAccessMeans(QontoGroupAuthenticationMeans authenticationMeans, QontoGroupProviderState providerState, QontoGroupHttpClient httpClient) throws TokenInvalidException {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add(REFRESH_TOKEN, providerState.getRefreshToken());
        tokenRequest.add(CLIENT_ID, authenticationMeans.getClientId());
        tokenRequest.add(CLIENT_SECRET, authenticationMeans.getClientSecret());
        tokenRequest.add(GRANT_TYPE, REFRESH_TOKEN);
        var tokenResponse = httpClient.createToken(tokenRequest);
        return new QontoGroupProviderState(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), Instant.now(clock).plusSeconds(tokenResponse.getExpiresInSeconds()).toEpochMilli());
    }

    private void verifyRedirectUrl(String redirectUrlPostedBackFromSite) {
        if (redirectUrlPostedBackFromSite.contains(ERROR)) {
            throw new IllegalStateException("redirectUrlPostedBackFromSite contains information about error");
        }
        String code = getCode(redirectUrlPostedBackFromSite);

        if (StringUtils.isEmpty(code)) {
            throw new IllegalStateException("redirectUrlPostedBackFromSite doesn't contain code");
        }
    }

    private String getCode(String redirectUrlPostedBackFromSite) {
        return UriComponentsBuilder.fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams().toSingleValueMap()
                .get(OAuth.CODE);
    }
}
