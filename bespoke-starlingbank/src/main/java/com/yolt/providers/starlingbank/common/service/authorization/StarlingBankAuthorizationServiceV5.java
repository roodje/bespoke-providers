package com.yolt.providers.starlingbank.common.service.authorization;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.configuration.StarlingBankProperties;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.http.authorizationurlparametersproducer.StarlingBankAuthorizationUrlParametersProducer;
import com.yolt.providers.starlingbank.common.mapper.StarlingBankTokenMapper;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;
import java.util.UUID;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class StarlingBankAuthorizationServiceV5 implements StarlingBankAuthorizationService {

    private final StarlingBankProperties properties;
    private final StarlingBankTokenMapper tokenMapper;
    private final StarlingBankAuthorizationUrlParametersProducer authorizationUrlParametersProducer;

    @Override
    public String getLoginUrl(String redirectUrl, String loginState, StarlingBankAuthenticationMeans authMeans) {
        MultiValueMap<String, String> params = authorizationUrlParametersProducer.createAuthorizationUrlParameters(redirectUrl, loginState, authMeans);

        return UriComponentsBuilder.fromHttpUrl(properties.getOAuthAuthorizationBaseUrl())
                .queryParams(params)
                .build()
                .toUriString();
    }

    @Override
    public AccessMeansDTO createAccessMeans(StarlingBankHttpClient httpClient,
                                            StarlingBankAuthenticationMeans authMeans,
                                            String redirectUrlPostedBackFromSite,
                                            UUID userId) {
        Token oAuthToken = getOAuthToken(httpClient, authMeans, redirectUrlPostedBackFromSite);
        return tokenMapper.mapToAccessMeansDTO(userId, oAuthToken);
    }

    @Override
    public Token getOAuthToken(StarlingBankHttpClient httpClient,
                               StarlingBankAuthenticationMeans authMeans,
                               String redirectUrlPostedBackFromSite) {
        String authorizationCode = UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(CODE);

        if (Objects.isNull(authorizationCode)) {
            throw new MissingDataException("Missing authorization code in redirect url query parameters");
        }
        String redirectUrl = UriComponentsBuilder
                .fromHttpUrl(redirectUrlPostedBackFromSite)
                .replaceQueryParams(null)
                .toUriString();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(CLIENT_ID, authMeans.getApiKey());
        body.add(CLIENT_SECRET, authMeans.getApiSecret());
        body.add(GRANT_TYPE, AUTHORIZATION_CODE);
        body.add(REDIRECT_URI, redirectUrl);
        body.add(CODE, authorizationCode);
        try {
            return httpClient.grantToken(properties.getOAuthTokenUrl(), body);
        } catch (TokenInvalidException | RuntimeException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }



    @Override
    public AccessMeansDTO refreshAccessMeans(StarlingBankHttpClient httpClient,
                                             AccessMeansDTO accessMeansDTO,
                                             StarlingBankAuthenticationMeans authMeans) throws TokenInvalidException {
        Token oldOAuthToken = tokenMapper.mapToToken(accessMeansDTO);
        UUID userId = accessMeansDTO.getUserId();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(CLIENT_ID, authMeans.getApiKey());
        body.add(CLIENT_SECRET, authMeans.getApiSecret());
        body.add(GRANT_TYPE, REFRESH_TOKEN);
        body.add(REFRESH_TOKEN, oldOAuthToken.getRefreshToken());
        try {
            Token oAuthToken = httpClient.refreshToken(properties.getOAuthTokenUrl(), body);
            if (oAuthToken == null) {
                throw new GetAccessTokenFailedException("Missing access token");
            }
            return tokenMapper.mapToAccessMeansDTO(userId, oAuthToken);
        } catch (RuntimeException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }
    @Override
    public Token getOAuthRefreshToken(StarlingBankHttpClient httpClient,
                                      String refreshToken,
                                      StarlingBankAuthenticationMeans authMeans) throws TokenInvalidException {

        Token oAuthToken;
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(CLIENT_ID, authMeans.getApiKey());
        body.add(CLIENT_SECRET, authMeans.getApiSecret());
        body.add(GRANT_TYPE, REFRESH_TOKEN);
        body.add(REFRESH_TOKEN, refreshToken);
        try {
            oAuthToken = httpClient.refreshToken(properties.getOAuthTokenUrl(), body);
            if (oAuthToken == null) {
                throw new GetAccessTokenFailedException("Missing access token");
            }
            return oAuthToken;
        } catch (RuntimeException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }
}
