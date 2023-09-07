package com.yolt.providers.stet.bnpparibasgroup.common.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.http.BnpParibasGroupToken;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Date;
import java.util.UUID;

public class BnpParibasGroupSampleAccessMeans {

    private BnpParibasGroupToken createToken(final String accessToken, final UUID userId) {
        BnpParibasGroupToken token = new BnpParibasGroupToken();
        token.setUserId(userId.toString());
        token.setAccessToken(accessToken);
        token.setRefreshToken("refresh-token");
        token.setExpiresIn(300);
        return token;
    }

    public AccessMeansDTO createAccessMeans(final String accessToken, final UUID userId) throws JsonProcessingException {
        BnpParibasGroupToken oAuthToken = createToken(accessToken, userId);
        String serializedOAuthToken = new ObjectMapper().writeValueAsString(oAuthToken);
        return new AccessMeansDTO(userId, serializedOAuthToken, new Date(), new Date());

    }
}
