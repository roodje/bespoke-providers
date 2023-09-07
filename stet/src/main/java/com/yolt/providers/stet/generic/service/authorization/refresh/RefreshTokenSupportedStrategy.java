package com.yolt.providers.stet.generic.service.authorization.refresh;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.AuthorizationRestClient;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@RequiredArgsConstructor
public class RefreshTokenSupportedStrategy implements RefreshTokenStrategy {

    protected final ProviderStateMapper providerStateMapper;
    protected final Scope refreshTokenScope;
    protected final AuthorizationRestClient restClient;
    protected final DateTimeSupplier dateTimeSupplier;

    @Override
    public AccessMeansDTO refreshAccessMeans(HttpClient httpClient, AccessMeansRequest request) throws TokenInvalidException {
        DataProviderState providerState = request.getProviderState();
        Region region = providerState.getRegion();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(
                region.getTokenUrl(),
                request.getAuthMeans(),
                providerState.getRefreshToken(),
                refreshTokenScope,
                request.getSigner());

        TokenResponseDTO token = restClient.refreshAccessToken(httpClient, refreshTokenRequest, request.getAuthMeans(), TokenResponseDTO.class);

        String refreshToken = StringUtils.defaultIfEmpty(token.getRefreshToken(), providerState.getRefreshToken());
        DataProviderState updatedProviderState = DataProviderState.authorizedProviderState(region, token.getAccessToken(), refreshToken, true);

        String jsonProviderState = providerStateMapper.mapToJson(updatedProviderState);
        return new AccessMeansDTO(
                request.getAccessMeans().getUserId(),
                jsonProviderState,
                Date.from(dateTimeSupplier.getDefaultInstant()),
                getExpirationDate(token.getExpiresIn()));
    }

    protected Date getExpirationDate(long expiresIn) {
        return Date.from(dateTimeSupplier.getDefaultInstant().plusSeconds(expiresIn));
    }
}
