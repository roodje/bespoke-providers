package com.yolt.providers.openbanking.ais.virginmoney2group.common.model;

import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
public class VirginMoney2GroupAccessMeans extends AccessMeans {

    //TODO C4PO-11529 remove this field when verified that VM introduced behavior mentioned in C4PO-9893
    //One refresh token issued during one final SCA (grant_type=authorization_code)
    private long consentExpirationTime;

    public VirginMoney2GroupAccessMeans(Instant created,
                                        UUID userId,
                                        String accessToken,
                                        String refreshToken,
                                        Date expireTime,
                                        Date updated,
                                        String redirectUri,
                                        long consentExpirationTime) {
        super(created, userId, accessToken, refreshToken, expireTime, updated, redirectUri);
        this.consentExpirationTime = consentExpirationTime;
    }
}
