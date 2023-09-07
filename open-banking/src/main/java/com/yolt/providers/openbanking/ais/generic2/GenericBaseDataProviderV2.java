package com.yolt.providers.openbanking.ais.generic2;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.ConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.LoginInfoStateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Slf4j
public class GenericBaseDataProviderV2<T extends LoginInfoState, U extends AccessMeansState> implements UrlDataProvider {

    private final FetchDataServiceV2 fetchDataService;
    private final AccountRequestService accountRequestService;
    private final AuthenticationService authenticationService;
    private final HttpClientFactory httpClientFactory;
    private final TokenScope scope;
    private final ProviderIdentification providerIdentification;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier;
    private final AccessMeansStateMapper<U> accessMeansStateMapper;
    private final AccessMeansStateProvider<U> accessMeansStateProvider;
    private final Supplier<Optional<KeyRequirements>> getSigningKeyRequirements;
    private final Supplier<Optional<KeyRequirements>> getTransportKeyRequirements;
    private final Supplier<ConsentValidityRules> consentValidityRulesSupplier;
    private final LoginInfoStateMapper<T> loginInfoStateMapper;
    private final Function<List<String>, T> loginInfoStateProvider;
    private final ConsentPermissions consentPermissions;


    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(request.getAuthenticationMeans());
        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());
        return fetchDataService.getAccountsAndTransactions(httpClient,
                authenticationMeans,
                request.getTransactionsFetchStartTime(),
                accessMeansStateMapper.fromJson(request.getAccessMeans().getAccessMeans()));
    }

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest request) {
        String secretState = request.getState();
        String redirectUrl = request.getBaseClientRedirectUrl();
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(request.getAuthenticationMeans());
        try {
            HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());
            String accountRequestId = accountRequestService.requestNewAccountRequestId(httpClient, authenticationMeans,
                    request.getAuthenticationMeansReference(), scope, request.getSigner());
            String authorizationUrl = authenticationService.generateAuthorizationUrl(authenticationMeans,
                    accountRequestId, secretState, redirectUrl, scope, request.getSigner());
            String providerState = loginInfoStateMapper.toJson(loginInfoStateProvider.apply(consentPermissions.getPermissions()));
            return new RedirectStep(authorizationUrl, accountRequestId,
                    providerState);
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request) {
        String redirectUrl = request.getRedirectUrlPostedBackFromSite();
        LoginInfoState loginInfoState = loginInfoStateMapper.fromJson(request.getProviderState());
        final UriComponents uriComponents = UriComponentsBuilder
                .fromUriString(request.getRedirectUrlPostedBackFromSite())
                .build();
        final Map<String, String> queryAndFragmentParameters = uriComponents
                .getQueryParams()
                .toSingleValueMap();

        if (uriComponents.getFragment() != null && !uriComponents.getFragment().isEmpty()) {
            queryAndFragmentParameters.putAll(getMapFromFragmentString(uriComponents.getFragment()));
        }


        final String error = queryAndFragmentParameters.get("error");
        if (!StringUtils.isEmpty(error)) {
            // In this case we want to log the redirect URL, because we want to know what went wrong and why.
            // The redirect URL shouldn't contain any sensitive data at this point, because the login was not successful.
            // Also, we return 'TOKEN_INVALID', because of the behavior that the app has for that reason.
            // See the JavaDoc on the enum value for more information.
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrl);
        }

        final String authorizationCode = queryAndFragmentParameters.get("code");
        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }

        // Fix for when banks suddenly decide to use a # instead of a ? to designate the start of the query parameters..
        int queryParamStartIndex = redirectUrl.indexOf('?');
        if (queryParamStartIndex == -1) {
            queryParamStartIndex = redirectUrl.indexOf('#');
        }

        redirectUrl = redirectUrl.substring(0, queryParamStartIndex);

        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(request.getAuthenticationMeans());

        try {
            HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

            AccessMeans accessToken = authenticationService.createAccessToken(httpClient, authenticationMeans,
                    request.getUserId(), authorizationCode, redirectUrl, scope, request.getSigner());
            String accessMeansStateValue = accessMeansStateMapper.toJson(
                    accessMeansStateProvider.apply(accessToken, loginInfoState.getPermissions()));
            AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                    request.getUserId(),
                    accessMeansStateValue,
                    new Date(),
                    accessToken.getExpireTime()
            );
            return new AccessMeansOrStepDTO(accessMeansDTO);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e.getMessage());
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        AccessMeansState oldAccessMeansState = accessMeansStateMapper.fromJson(request.getAccessMeans().getAccessMeans());
        AccessMeans oAuthToken = oldAccessMeansState.getAccessMeans();
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply((request.getAuthenticationMeans()));

        if (StringUtils.isEmpty(oAuthToken.getRefreshToken())) {
            throw new TokenInvalidException("Refresh token is missing, and access token is expired.");
        }

        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        AccessMeans newOAuthToken = authenticationService.refreshAccessToken(httpClient, authenticationMeans,
                oAuthToken.getUserId(), oAuthToken.getRefreshToken(), oAuthToken.getRedirectUri(), scope,
                request.getSigner());
        String accessMeansState = accessMeansStateMapper.toJson(
                (U) accessMeansStateProvider.apply(newOAuthToken, oldAccessMeansState.getPermissions()));
        return new AccessMeansDTO(
                request.getAccessMeans().getUserId(),
                accessMeansState,
                new Date(),
                newOAuthToken.getExpireTime()
        );
    }

    @Override
    public void onUserSiteDelete(final UrlOnUserSiteDeleteRequest request) throws TokenInvalidException {
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply((request.getAuthenticationMeans()));
        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        accountRequestService.deleteAccountRequest(httpClient, authenticationMeans,
                request.getAuthenticationMeansReference(),
                request.getExternalConsentId(), scope, request.getSigner());
    }

    protected Map<String, String> getMapFromFragmentString(String queryString) {
        String[] queryParams = queryString.split("&");
        Map<String, String> mappedQueryParams = new HashMap<>();
        for (String queryParam : queryParams) {
            String[] keyValue = queryParam.split("=");
            String value = keyValue.length == 2 ? keyValue[1] : null;
            mappedQueryParams.put(keyValue[0], value);
        }
        return mappedQueryParams;
    }

    @Override
    public final Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthenticationMeansSupplier.get();
    }

    @Override
    public final String getProviderIdentifier() {
        return providerIdentification.getIdentifier();
    }

    @Override
    public final String getProviderIdentifierDisplayName() {
        return providerIdentification.getDisplayName();
    }

    @Override
    public final ProviderVersion getVersion() {
        return providerIdentification.getVersion();
    }

    @Override
    public final Optional<KeyRequirements> getSigningKeyRequirements() {
        return getSigningKeyRequirements.get();
    }

    @Override
    public final Optional<KeyRequirements> getTransportKeyRequirements() {
        return getTransportKeyRequirements.get();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return consentValidityRulesSupplier.get();
    }

}
