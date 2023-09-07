package com.yolt.providers.stet.generic.service.authorization;

import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenStrategy;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansOrStepRequest;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansRequest;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.AuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationRedirectUrlSupplier;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Date;

public class SingleRegionAuthorizationService implements AuthorizationService {

    protected final RefreshTokenStrategy refreshTokenStrategy;
    protected final AuthorizationRestClient restClient;
    protected final ProviderStateMapper providerStateMapper;
    protected final Scope accessTokenScope;
    protected final DefaultProperties properties;
    protected final AuthorizationCodeExtractor authCodeExtractor;
    protected final AuthorizationRedirectUrlSupplier authRedirectUrlSupplier;
    protected final DateTimeSupplier dateTimeSupplier;

    public SingleRegionAuthorizationService(RefreshTokenStrategy refreshTokenStrategy,
                                            AuthorizationRestClient restClient,
                                            ProviderStateMapper providerStateMapper,
                                            Scope accessTokenScope,
                                            DefaultProperties properties,
                                            AuthorizationCodeExtractor authCodeExtractor,
                                            AuthorizationRedirectUrlSupplier authRedirectUrlSupplier,
                                            DateTimeSupplier dateTimeSupplier) {
        this.refreshTokenStrategy = refreshTokenStrategy;
        this.restClient = restClient;
        this.providerStateMapper = providerStateMapper;
        this.accessTokenScope = accessTokenScope;
        this.properties = properties;
        this.authCodeExtractor = authCodeExtractor;
        this.authRedirectUrlSupplier = authRedirectUrlSupplier;
        this.dateTimeSupplier = dateTimeSupplier;
    }

    protected Region getRegion(String regionCode) { //NOSONAR It allows others to use it
        return properties.getRegions().get(0);
    }

    @Override
    public Step getStep(StepRequest request) {
        Region region = getRegion(request.getRegionCode());
        AuthorizationRedirect authorizationRedirect = authRedirectUrlSupplier.createAuthorizationRedirectUrl(
                region.getAuthUrl(),
                accessTokenScope,
                request);

        String jsonProviderState = providerStateMapper.mapToJson(DataProviderState.preAuthorizedProviderState(region, authorizationRedirect));
        return new RedirectStep(authorizationRedirect.getUrl(), null, jsonProviderState);
    }

    @Override
    public AccessMeansOrStepDTO createAccessMeansOrGetStep(HttpClient httpClient, AccessMeansOrStepRequest request) throws TokenInvalidException {
        DataProviderState providerState = providerStateMapper.mapToDataProviderState(request.getProviderState());
        String authorizationCode = authCodeExtractor.extractAuthorizationCode(request.getRedirectUrlPostedBackFromSite());
        Region region = providerState.getRegion();
        String redirectUrl = createAccessTokenRequestRedirectUrl(request);

        AccessTokenRequest accessTokenRequest = new AccessTokenRequest(
                region.getTokenUrl(),
                request.getAuthMeans(),
                authorizationCode,
                redirectUrl,
                providerState,
                accessTokenScope,
                request.getSigner());

        TokenResponseDTO token = restClient.getAccessToken(httpClient, accessTokenRequest, request.getAuthMeans(), TokenResponseDTO.class);
        DataProviderState updatedProviderState = DataProviderState.authorizedProviderState(region, token.getAccessToken(), token.getRefreshToken());

        String jsonProviderState = providerStateMapper.mapToJson(updatedProviderState);
        return new AccessMeansOrStepDTO(new AccessMeansDTO(
                request.getUserId(),
                jsonProviderState,
                Date.from(dateTimeSupplier.getDefaultInstant()),
                getExpirationDate(token.getExpiresIn())));
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(HttpClient httpClient, AccessMeansRequest request) throws TokenInvalidException {
        return refreshTokenStrategy.refreshAccessMeans(httpClient, request);
    }

    protected Date getExpirationDate(long expiresIn) {
        return Date.from(dateTimeSupplier.getDefaultInstant().plusSeconds(expiresIn));
    }

    public String createAccessTokenRequestRedirectUrl(AccessMeansOrStepRequest request) {
        return request.getBaseClientRedirectUrl();
    }
}
