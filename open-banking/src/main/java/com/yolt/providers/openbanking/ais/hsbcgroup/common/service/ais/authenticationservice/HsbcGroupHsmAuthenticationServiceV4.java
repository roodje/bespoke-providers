package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.authenticationservice;

import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class HsbcGroupHsmAuthenticationServiceV4 extends DefaultAuthenticationService {

    final Clock clock;

    public HsbcGroupHsmAuthenticationServiceV4(String oauthAuthorizationUrl,
                                               Oauth2Client oauth2Client,
                                               UserRequestTokenSigner userRequestTokenSigner,
                                               TokenClaimsProducer tokenClaimsProducer,
                                               Clock clock) {
        super(oauthAuthorizationUrl, oauth2Client, userRequestTokenSigner, tokenClaimsProducer, clock);
        this.clock = clock;
    }

    @Override
    protected AccessMeans convertToOAuthToken(final UUID userId,
                                              final AccessTokenResponseDTO accessTokenResponseDTO,
                                              final String redirectURI,
                                              final Instant createDate) {
        final long expiresInSeconds = accessTokenResponseDTO.getExpiresIn();
        final Instant expireInstant = Instant.now(clock).plusSeconds(expiresInSeconds);

        final AccessMeans oAuthToken = new HsbcGroupAccessMeansV2();
        oAuthToken.setUserId(userId);
        oAuthToken.setAccessToken(accessTokenResponseDTO.getAccessToken());
        oAuthToken.setExpireTime(Date.from(expireInstant));
        oAuthToken.setUpdated(new Date());
        oAuthToken.setRefreshToken(accessTokenResponseDTO.getRefreshToken());
        oAuthToken.setRedirectUri(redirectURI);
        oAuthToken.setCreated(createDate);
        return oAuthToken;
    }
}
