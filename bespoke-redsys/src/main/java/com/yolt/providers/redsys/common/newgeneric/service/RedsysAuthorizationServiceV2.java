package com.yolt.providers.redsys.common.newgeneric.service;

import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import com.yolt.providers.redsys.common.dto.RequestGetConsent;
import com.yolt.providers.redsys.common.dto.ResponseGetConsent;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.common.newgeneric.ConsentProcessArguments;
import com.yolt.providers.redsys.common.newgeneric.SerializableConsentProcessData;
import com.yolt.providers.redsys.common.newgeneric.rest.RedsysHttpClientV2;
import com.yolt.providers.redsys.common.newgeneric.rest.RestTemplateService;
import com.yolt.providers.redsys.common.service.RedsysConsentObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

import static com.yolt.providers.redsys.common.util.ErrorHandlerUtil.handleNon2xxResponseInAuthorization;

@RequiredArgsConstructor
public class RedsysAuthorizationServiceV2<T extends SerializableConsentProcessData> {

    private final RestTemplateService restTemplateService;
    private final RedsysConsentObjectService consentObjectService;
    private final RedsysBaseProperties properties;
    private final RedsysHttpClientV2 httpClient;

    public ResponseGetConsent getConsentId(final ConsentProcessArguments<T> processArguments,
                                           final String userAccessToken,
                                           final LocalDate validUntil,
                                           final String redirectUrlWithState) {

        RequestGetConsent consentObject = consentObjectService.getConsentObject(validUntil, processArguments.getConsentProcessData().getAccessMeans().getFormValues());

        return httpClient.generateConsent(
                restTemplateService.createRestTemplate(processArguments.getAuthenticationMeans(), processArguments.getRestTemplateManager()),
                consentObject,
                userAccessToken,
                processArguments.getAuthenticationMeans().getSigningData(processArguments.getSigner()),
                processArguments.getPsuIpAddress(),
                redirectUrlWithState,
                processArguments.getConsentProcessData().getAspspName());
    }

    public Token createAccessToken(final ConsentProcessArguments<T> processArguments) {

        String redirectUrl = processArguments.getRedirectUriPostedBackFromSite();
        final String authorizationCode = UriComponentsBuilder
                .fromUriString(redirectUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(OAuth.CODE);

        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing authorization code");
        }

        T processData = processArguments.getConsentProcessData();
        return httpClient.getAccessToken(
                restTemplateService.createRestTemplate(processArguments.getAuthenticationMeans(), processArguments.getRestTemplateManager()),
                processArguments.getAuthenticationMeans(),
                redirectUrl.substring(0, redirectUrl.indexOf('?')),
                authorizationCode,
                processData.getAccessMeans().getCodeVerifier(),
                properties.getAuthorizationUrl(),
                processData.getAspspName());
    }

    public Token createNewAccessTokenFromRefreshToken(final RestTemplateManager restTemplateManager,
                                                      final UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest,
                                                      final RedsysAuthenticationMeans authenticationMeans,
                                                      final SerializableConsentProcessData serializableConsentProcessData) throws TokenInvalidException {
        try {
            return httpClient.getNewAccessTokenUsingRefreshToken(
                    restTemplateService.createRestTemplate(authenticationMeans, restTemplateManager),
                    authenticationMeans,
                    serializableConsentProcessData.getAccessMeans().getToken().getRefreshToken(),
                    properties.getAuthorizationUrl(),
                    urlRefreshAccessMeansRequest.getPsuIpAddress(),
                    serializableConsentProcessData.getAspspName());
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseInAuthorization(e, urlRefreshAccessMeansRequest.getPsuIpAddress());
            throw e;
        }
    }
}
