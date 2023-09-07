package com.yolt.providers.redsys.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import com.yolt.providers.redsys.common.dto.RequestGetConsent;
import com.yolt.providers.redsys.common.dto.ResponseGetConsent;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.common.rest.RedsysHttpClient;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

import static com.yolt.providers.redsys.common.util.ErrorHandlerUtil.handleNon2xxResponseInAuthorization;

@RequiredArgsConstructor
public class RedsysAuthorizationService {

    private final RedsysRestTemplateService restTemplateService;
    private final RedsysConsentObjectService consentObjectService;
    private final RedsysBaseProperties properties;

    public ResponseGetConsent getConsentId(final UrlCreateAccessMeansRequest urlRequest,
                                           final RedsysAuthenticationMeans authenticationMeans,
                                           final String userAccessToken,
                                           final LocalDate validUntil,
                                           final String redirectUrlWithState,
                                           final FilledInUserSiteFormValues filledInUserSiteFormValues) {

        RequestGetConsent consentObject = consentObjectService.getConsentObject(validUntil, filledInUserSiteFormValues);

        RedsysHttpClient httpClient = restTemplateService.createHttpClient(authenticationMeans, urlRequest.getRestTemplateManager());
        return httpClient.generateConsent(
                consentObject,
                userAccessToken,
                authenticationMeans.getSigningData(urlRequest.getSigner()),
                urlRequest.getPsuIpAddress(),
                redirectUrlWithState);
    }

    public Token createAccessToken(final UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                   final String redirectUrl,
                                   final RedsysAuthenticationMeans authenticationMeans,
                                   final String codeVerifier) {

        final String authorizationCode = UriComponentsBuilder
                .fromUriString(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap().get("code");

        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing authorization code");
        }

        RedsysHttpClient httpClient = restTemplateService.createHttpClient(authenticationMeans, urlCreateAccessMeans.getRestTemplateManager());
        return httpClient.getAccessToken(
                authenticationMeans,
                redirectUrl,
                authorizationCode,
                codeVerifier,
                properties.getAuthorizationUrl());
    }

    public Token createNewAccessTokenFromRefreshToken(final UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest,
                                                      final RedsysAccessMeans accessMeansDTO,
                                                      final RedsysAuthenticationMeans authenticationMeans) throws TokenInvalidException {
        RedsysHttpClient httpClient = restTemplateService.createHttpClient(authenticationMeans, urlRefreshAccessMeansRequest.getRestTemplateManager());
        try {
            return httpClient.getNewAccessTokenUsingRefreshToken(
                    authenticationMeans,
                    accessMeansDTO.getToken().getRefreshToken(),
                    properties.getAuthorizationUrl(),
                    urlRefreshAccessMeansRequest.getPsuIpAddress());
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseInAuthorization(e, urlRefreshAccessMeansRequest.getPsuIpAddress());
            throw e;
        }
    }
}
