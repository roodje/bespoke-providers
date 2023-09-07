package com.yolt.providers.openbanking.ais.virginmoney2group.common.service;

import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.model.VirginMoney2GroupAccessMeans;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class VirginMoney2GroupAuthenticationServiceV1 extends DefaultAuthenticationService {

    private static final long CONSENT_VALIDITY_TIME = 7776000L; // consent is valid for 90 days

    final Clock clock;

    public VirginMoney2GroupAuthenticationServiceV1(String oauthAuthorizationUrl,
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

        return new VirginMoney2GroupAccessMeans(createDate, userId, accessTokenResponseDTO.getAccessToken(),
                accessTokenResponseDTO.getRefreshToken(), Date.from(expireInstant), new Date(), redirectURI,
                Instant.now(clock).plusSeconds(CONSENT_VALIDITY_TIME).toEpochMilli());
    }
}
