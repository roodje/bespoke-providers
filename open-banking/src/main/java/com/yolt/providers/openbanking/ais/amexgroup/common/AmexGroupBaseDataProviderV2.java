package com.yolt.providers.openbanking.ais.amexgroup.common;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.amexgroup.common.domain.AmexLoginInfoState;
import com.yolt.providers.openbanking.ais.amexgroup.common.pkce.PKCE;
import com.yolt.providers.openbanking.ais.amexgroup.common.service.AmexGroupAuthenticationService;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.ConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.LoginInfoStateMapper;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class AmexGroupBaseDataProviderV2 extends GenericBaseDataProviderV2<AmexLoginInfoState, AccessMeansState> {

    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final HttpClientFactory httpClientFactory;
    private final AccountRequestService accountRequestService;
    private final TokenScope scope;
    private final AmexGroupAuthenticationService authenticationService;
    private final LoginInfoStateMapper<AmexLoginInfoState> loginInfoStateMapper;
    private final Function<List<String>, AmexLoginInfoState> loginInfoStateProvider;
    private final ConsentPermissions consentPermissions;
    private final PKCE pkce;
    private final AccessMeansStateMapper<AccessMeansState> accessMeansStateMapper;
    private final AccessMeansStateProvider<AccessMeansState> accessMeansStateProvider;

    public AmexGroupBaseDataProviderV2(FetchDataServiceV2 fetchDataService,
                                       AccountRequestService accountRequestService,
                                       AmexGroupAuthenticationService authenticationService,
                                       HttpClientFactory httpClientFactory,
                                       TokenScope scope,
                                       ProviderIdentification providerIdentification,
                                       Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                       Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                       AccessMeansStateMapper<AccessMeansState> accessMeansStateMapper,
                                       AccessMeansStateProvider<AccessMeansState> accessMeansStateProvider,
                                       Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                       Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                       Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                       LoginInfoStateMapper<AmexLoginInfoState> loginInfoStateMapper,
                                       Function<List<String>, AmexLoginInfoState> loginInfoStateProvider,
                                       ConsentPermissions consentPermissions,
                                       PKCE pkce) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification, getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansStateMapper, accessMeansStateProvider, getSigningKeyRequirements, getTransportKeyRequirements, consentValidityRulesSupplier, loginInfoStateMapper, loginInfoStateProvider, consentPermissions);
        this.getAuthenticationMeans = getAuthenticationMeans;
        this.httpClientFactory = httpClientFactory;
        this.accountRequestService = accountRequestService;
        this.scope = scope;
        this.authenticationService = authenticationService;
        this.loginInfoStateMapper = loginInfoStateMapper;
        this.loginInfoStateProvider = loginInfoStateProvider;
        this.consentPermissions = consentPermissions;
        this.pkce = pkce;
        this.accessMeansStateMapper = accessMeansStateMapper;
        this.accessMeansStateProvider = accessMeansStateProvider;
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
            OAuth2ProofKeyCodeExchange codeExchange = pkce.createRandomS256();
            String authorizationUrl = authenticationService.generateAuthorizationUrl(authenticationMeans,
                    accountRequestId, secretState, redirectUrl, scope, codeExchange.getCodeChallenge(), codeExchange.getCodeChallengeMethod());
            AmexLoginInfoState amexLoginInfoState = loginInfoStateProvider.apply(consentPermissions.getPermissions());
            amexLoginInfoState.setCodeVerifier(codeExchange.getCodeVerifier());
            String providerState = loginInfoStateMapper.toJson(amexLoginInfoState);
            return new RedirectStep(authorizationUrl, accountRequestId,
                    providerState);
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request) {
        String redirectUrl = request.getRedirectUrlPostedBackFromSite();
        AmexLoginInfoState loginInfoState = loginInfoStateMapper.fromJson(request.getProviderState());
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

        final String authorizationCode = queryAndFragmentParameters.get("authtoken");
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
                    request.getUserId(), authorizationCode, redirectUrl, scope, loginInfoState.getCodeVerifier());
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
}
