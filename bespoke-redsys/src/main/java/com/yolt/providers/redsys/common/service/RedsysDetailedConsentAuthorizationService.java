package com.yolt.providers.redsys.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.common.rest.RedsysHttpClient;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class RedsysDetailedConsentAuthorizationService extends RedsysAuthorizationService {

    private final RedsysRestTemplateService restTemplateService;
    private final RedsysBaseProperties properties;

    public RedsysDetailedConsentAuthorizationService(RedsysRestTemplateService restTemplateService,
                                                     RedsysConsentObjectService consentObjectService,
                                                     RedsysBaseProperties properties) {
        super(restTemplateService, consentObjectService, properties);
        this.restTemplateService = restTemplateService;
        this.properties = properties;
    }

    public Token createAccessToken(final UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                   final String redirectUrl,
                                   final RedsysAuthenticationMeans authenticationMeans,
                                   final String codeVerifier,
                                   final String redirectUrlPostedBackFromSite) {

        final String authorizationCode = UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
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
}
