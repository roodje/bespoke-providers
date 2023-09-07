package com.yolt.providers.openbanking.ais.virginmoney2group.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
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
import com.yolt.providers.openbanking.ais.virginmoney2group.common.model.VirginMoney2GroupAccessMeans;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.service.VirginMoney2GroupAutoOnboardingService;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class VirginMoney2DataProviderV1 extends GenericBaseDataProviderV2<LoginInfoState, AccessMeansState<AccessMeans>> implements AutoOnboardingProvider {

    private final AuthenticationService authenticationService;
    private final AccessMeansStateMapper accessMeansStateMapper;
    private final AccessMeansStateProvider accessMeansStateProvider;
    private final HttpClientFactory httpClientFactory;
    private final TokenScope scope;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final VirginMoney2GroupAutoOnboardingService autoOnboardingService;
    private final Clock clock;

    public VirginMoney2DataProviderV1(FetchDataServiceV2 fetchDataService,
                                      AccountRequestService accountRequestService,
                                      AuthenticationService authenticationService,
                                      HttpClientFactory httpClientFactory,
                                      TokenScope scope,
                                      ProviderIdentification providerIdentification,
                                      Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                      Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                      AccessMeansStateMapper accessMeansStateMapper,
                                      AccessMeansStateProvider accessMeansStateProvider,
                                      Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                      Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                      Supplier<ConsentValidityRules> consentValidityRulesSupplier,
                                      LoginInfoStateMapper<LoginInfoState> loginInfoStateMapper,
                                      Function<List<String>, LoginInfoState> loginInfoStateProvider,
                                      ConsentPermissions consentPermissions,
                                      VirginMoney2GroupAutoOnboardingService autoOnboardingService,
                                      Clock clock) {
        super(fetchDataService,
                accountRequestService,
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                getAuthenticationMeans,
                typedAuthenticationMeansSupplier,
                accessMeansStateMapper,
                accessMeansStateProvider,
                getSigningKeyRequirements,
                getTransportKeyRequirements,
                consentValidityRulesSupplier,
                loginInfoStateMapper,
                loginInfoStateProvider,
                consentPermissions);
        this.authenticationService = authenticationService;
        this.accessMeansStateMapper = accessMeansStateMapper;
        this.accessMeansStateProvider = accessMeansStateProvider;
        this.httpClientFactory = httpClientFactory;
        this.scope = scope;
        this.getAuthenticationMeans = getAuthenticationMeans;
        this.autoOnboardingService = autoOnboardingService;
        this.clock = clock;
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        AccessMeansState oldAccessMeansState = accessMeansStateMapper.fromJson(request.getAccessMeans().getAccessMeans());
        VirginMoney2GroupAccessMeans oAuthToken = (VirginMoney2GroupAccessMeans) oldAccessMeansState.getAccessMeans();
        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply((request.getAuthenticationMeans()));

        if (StringUtils.isEmpty(oAuthToken.getRefreshToken())) {
            throw new TokenInvalidException("Refresh token is missing, and access token is expired.");
        }
        HttpClient httpClient = httpClientFactory.createHttpClient(request.getRestTemplateManager(), authenticationMeans, getProviderIdentifierDisplayName());

        VirginMoney2GroupAccessMeans newOAuthToken;

        try {
            newOAuthToken = (VirginMoney2GroupAccessMeans) authenticationService.refreshAccessToken(httpClient, authenticationMeans,
                    oAuthToken.getUserId(), oAuthToken.getRefreshToken(), oAuthToken.getRedirectUri(), scope,
                    request.getSigner());
            newOAuthToken.setConsentExpirationTime(oAuthToken.getConsentExpirationTime());

        } catch (HttpClientErrorException e) {
            if (isConsentExpired(e, oAuthToken.getConsentExpirationTime())) {
                throw new TokenInvalidException("Bad request occurred, but user's consent has expired");
            }
            throw e;
        }

        String accessMeansState = accessMeansStateMapper.toJson(
                accessMeansStateProvider.apply(newOAuthToken, oldAccessMeansState.getPermissions()));
        return new AccessMeansDTO(
                request.getAccessMeans().getUserId(),
                accessMeansState,
                new Date(),
                newOAuthToken.getExpireTime()
        );
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return autoOnboardingService.getAutoConfiguredMeans(getTypedAuthenticationMeans());
    }

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        return autoOnboardingService.register(urlAutoOnboardingRequest);
    }

    private boolean isConsentExpired(final HttpStatusCodeException e, long consentExpirationTime) {
        return e.getStatusCode().equals(HttpStatus.BAD_REQUEST) && (consentExpirationTime == 0 || Instant.ofEpochMilli(consentExpirationTime).isBefore(Instant.now(clock)));
    }
}
