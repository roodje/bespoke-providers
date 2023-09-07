package com.yolt.providers.monorepogroup.chebancagroup.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.chebancagroup.common.CheBancaGroupProperties;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.ChaBancaGroupAccessMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.CheBancaGroupToken;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.CheBancaGroupHttpClient;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.CheBancaGroupTokenMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class DefaultCheBancaGroupAuthorizationService implements CheBancaGroupAuthorizationService {

    private final DefaultCheBancaGroupTokenService tokenService;
    private final CheBancaGroupProperties properties;
    private final CheBancaGroupTokenMapper tokenMapper;

    @Override
    public RedirectStep getLoginInfo(final CheBancaGroupHttpClient httpClient, final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String baseClientRedirectUrl, final String state) {
        try {
            String authorizationUrl = createAuthorizationUrl(
                    authenticationMeans.getClientId(),
                    baseClientRedirectUrl,
                    state);

            ResponseEntity<String> authorizationSession = httpClient.createAuthorizationSession(signer, authorizationUrl, authenticationMeans);

            return new RedirectStep(authorizationSession.getHeaders().getLocation().toString(), null, null);
        } catch (TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException("Failed to get authorization URL", e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final Signer signer,
                                                     final CheBancaGroupHttpClient httpClient,
                                                     final CheBancaGroupAuthenticationMeans authenticationMeans,
                                                     final UrlCreateAccessMeansRequest urlCreateAccessMeansRequest) {
        try {
            final String authorizationCode = getCodeFromAuthorizationRedirectUrl(urlCreateAccessMeansRequest);

            CheBancaGroupToken token = tokenService.createClientCredentialToken(
                    signer,
                    httpClient,
                    authenticationMeans,
                    urlCreateAccessMeansRequest.getBaseClientRedirectUrl(),
                    authorizationCode);

            ChaBancaGroupAccessMeans chaBancaGroupAccessMeans = new ChaBancaGroupAccessMeans(
                    token.getAccessToken(),
                    token.getTokenType(),
                    token.getTokenValidityTimeInSeconds(),
                    token.getRefreshToken(),
                    token.getRefreshTokenValidityTimeInSeconds(),
                    token.getScope());

            return new AccessMeansOrStepDTO(
                    tokenMapper.mapToAccessMeans(urlCreateAccessMeansRequest.getUserId(), chaBancaGroupAccessMeans));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Failed to create access means");
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final Signer signer,
                                             final CheBancaGroupHttpClient httpClient,
                                             final CheBancaGroupAuthenticationMeans authenticationMeans,
                                             final UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest) {
        try {
            ChaBancaGroupAccessMeans oldAccessMeans = tokenMapper.mapToToken(urlRefreshAccessMeansRequest.getAccessMeans());
            CheBancaGroupToken token = tokenService.createRefreshToken(
                    signer,
                    httpClient,
                    authenticationMeans,
                    oldAccessMeans.getRefreshToken());

            ChaBancaGroupAccessMeans chaBancaGroupAccessMeans = new ChaBancaGroupAccessMeans(
                    token.getAccessToken(),
                    token.getTokenType(),
                    token.getTokenValidityTimeInSeconds(),
                    token.getRefreshToken(),
                    token.getRefreshTokenValidityTimeInSeconds(),
                    token.getScope());

            return tokenMapper.mapToAccessMeans(urlRefreshAccessMeansRequest.getAccessMeans().getUserId(), chaBancaGroupAccessMeans);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Failed to refresh access means");
        }
    }

    private String createAuthorizationUrl(final String clientId,
                                          final String redirectUrl,
                                          final String state) {
        String urlEncoded = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
        return UriComponentsBuilder.fromHttpUrl(properties.getAuthorizeUrl())
                .queryParam(OAuth.RESPONSE_TYPE, OAuth.CODE)
                .queryParam(OAuth.CLIENT_ID, clientId)
                .queryParam(OAuth.STATE, state)
                .queryParam(OAuth.REDIRECT_URI, urlEncoded)
                .build(true)
                .toUriString();
    }

    private String getCodeFromAuthorizationRedirectUrl(final UrlCreateAccessMeansRequest urlCreateAccessMeansRequest) {
        return UriComponentsBuilder
                .fromUriString(urlCreateAccessMeansRequest.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap().get("code");
    }
}
