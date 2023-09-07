package com.yolt.providers.openbanking.ais.santander.service;

import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.santander.dto.SantanderAccessMeansV2;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class SantanderAuthenticationServiceV2 extends DefaultAuthenticationService {

    private final Clock clock;

    public SantanderAuthenticationServiceV2(String oauthAuthorizationUrl,
                                            Oauth2Client oauth2Client,
                                            UserRequestTokenSigner userRequestTokenSigner,
                                            TokenClaimsProducer tokenClaimsProducer,
                                            Clock clock) {
        super(oauthAuthorizationUrl, oauth2Client, userRequestTokenSigner, tokenClaimsProducer, clock);
        this.clock = clock;
    }

    @Override
    protected AccessMeans convertToOAuthToken(UUID userId,
                                              AccessTokenResponseDTO accessTokenResponseDTO,
                                              String redirectURI,
                                              Instant createDate) {
        final long expiresInSeconds = accessTokenResponseDTO.getExpiresIn();
        final Instant expireInstant = Instant.now(clock).plusSeconds(expiresInSeconds);

        return new SantanderAccessMeansV2(createDate, userId, accessTokenResponseDTO.getAccessToken(),
                accessTokenResponseDTO.getRefreshToken(), Date.from(expireInstant), new Date(), redirectURI);
    }
}
