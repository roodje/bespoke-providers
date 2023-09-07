package com.yolt.providers.amexgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeanProducer;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeans;
import com.yolt.providers.amexgroup.common.dto.TokenResponses;
import com.yolt.providers.amexgroup.common.service.AmexGroupAuthenticationService;
import com.yolt.providers.amexgroup.common.service.AmexGroupFetchDataService;
import com.yolt.providers.amexgroup.common.utils.HsmUtils;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
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
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.providers.amexgroup.common.utils.AmexAuthMeansFields.*;

@RequiredArgsConstructor
public class AmexGroupDataProviderV6 implements UrlDataProvider {

    private static final String AMEX_PROVIDER_IDENTIFIER_DISPLAY_NAME = "American Express Cards";

    private final AmexGroupConfigurationProperties properties;
    private final ObjectMapper objectMapper;
    private final AmexGroupAuthenticationService amexGroupAuthenticationService;
    private final AmexGroupFetchDataService amexGroupFetchDataService;
    private final AmexGroupAuthMeanProducer amexGroupAuthMeansProducer;
    @Getter
    private final String providerIdentifier;
    @Getter
    private final ProviderVersion version;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {

        TokenResponses tokens = toAccessTokens(urlFetchData.getAccessMeans().getAccessMeans());

        return amexGroupFetchDataService.getAccountsAndTransactions(
                amexGroupAuthMeansProducer.createAuthMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier()),
                urlFetchData.getRestTemplateManager(),
                tokens,
                urlFetchData.getTransactionsFetchStartTime());
    }

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest urlGetLogin) {

        AmexGroupAuthMeans authenticationMeans = amexGroupAuthMeansProducer.createAuthMeans(
                urlGetLogin.getAuthenticationMeans(),
                getProviderIdentifierDisplayName());

        String loginUrl = amexGroupAuthenticationService.getLoginInfo(
                authenticationMeans,
                urlGetLogin);
        /*
         * Amex requires implementation of on-user-site-delete in which we revoke current refresh token for users to be able to freely add and remove site. It is triggered only if external consent ID is available for site.
         * Unfortunately Amex only allows to revoke specific refresh token by its value instead of by consent Id, which is not supported by the bank.
         * To make sure on-user-site-delete is triggered in site-management we need to sent dummy consentId
         */
        String dummyExternalConsentId = UUID.randomUUID().toString();
        return new RedirectStep(loginUrl, dummyExternalConsentId, properties.getBaseUrl());
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        String redirectUrl = urlCreateAccessMeans.getRedirectUrlPostedBackFromSite();
        Map<String, String> queryAndFragmentParameters = UriComponentsBuilder
                .fromUriString(redirectUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        Map<String, String> caseInsensitiveQueryParameters = new LinkedCaseInsensitiveMap<>();
        caseInsensitiveQueryParameters.putAll(queryAndFragmentParameters);

        String csvAuthorizationCodes = getCsvAuthorizationCodesFromParameters(caseInsensitiveQueryParameters);

        return amexGroupAuthenticationService.createNewAccessMeansFromCsv(
                amexGroupAuthMeansProducer.createAuthMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifierDisplayName()),
                urlCreateAccessMeans,
                csvAuthorizationCodes);
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest request) throws TokenInvalidException {
        amexGroupAuthenticationService.revokeUserToken(
                request,
                amexGroupAuthMeansProducer.createAuthMeans(request.getAuthenticationMeans(), getProviderIdentifier()));
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        //C4PO-9807 new set of auth means for OB
        typedAuthenticationMeans.put(CLIENT_ID_2, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_2, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KID_2, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_2, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);

        return typedAuthenticationMeans;
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        return amexGroupAuthenticationService.getRefreshAccessMeans(
                urlRefreshAccessMeans,
                amexGroupAuthMeansProducer.createAuthMeans(
                        urlRefreshAccessMeans.getAuthenticationMeans(),
                        getProviderIdentifierDisplayName()));
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
//        return HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_ROTATION, CLIENT_TRANSPORT_CERTIFICATE_ROTATION);
//        C4PO-9807 new set of auth means
        return HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KID_2, TRANSPORT_CERTIFICATE_2);
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return AMEX_PROVIDER_IDENTIFIER_DISPLAY_NAME;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private TokenResponses toAccessTokens(String accessMeans) {
        try {
            return objectMapper.readValue(accessMeans, TokenResponses.class);
        } catch (IOException e) {
            return null;
        }
    }

    private String getCsvAuthorizationCodesFromParameters(Map<String, String> caseInsensitiveQueryParameters) {
        String csvAuthorizationCodes = caseInsensitiveQueryParameters.get("code");
        if (!StringUtils.hasText(csvAuthorizationCodes)) {
            return getCsvAuthorizationCodesFromAuthTokenParameter(caseInsensitiveQueryParameters);
        }
        for (String authorizationCode : csvAuthorizationCodes.split(",")) {
            if (!StringUtils.hasText(authorizationCode)) {
                return getCsvAuthorizationCodesFromAuthTokenParameter(caseInsensitiveQueryParameters);
            }
        }
        return csvAuthorizationCodes;
    }

    // TODO Remove support for authToken parameter in ticket C4PO-9737
    private String getCsvAuthorizationCodesFromAuthTokenParameter(Map<String, String> caseInsensitiveQueryParameters) {
        String csvAuthorizationCodes = caseInsensitiveQueryParameters.get("authtoken");
        if (!StringUtils.hasText(csvAuthorizationCodes)) {
            throw new GetAccessTokenFailedException("Missing authToken redirect url query parameter.");
        }
        for (String authorizationCode : csvAuthorizationCodes.split(",")) {
            if (!StringUtils.hasText(authorizationCode)) {
                throw new MissingDataException("Missing authorization code in redirect url query parameters");
            }
        }
        return csvAuthorizationCodes;
    }
}