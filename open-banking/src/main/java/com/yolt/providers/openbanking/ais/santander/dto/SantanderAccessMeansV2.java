package com.yolt.providers.openbanking.ais.santander.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SantanderAccessMeansV2 extends AccessMeans {

    @JsonProperty("redirecturi")
    private String redirectUri;

    public SantanderAccessMeansV2(Instant created, UUID userId, String accessToken, String refreshToken, Date expireTime, Date updated, String redirectUri){
        super(created, userId, accessToken, refreshToken, expireTime, updated, redirectUri);
        this.redirectUri = redirectUri;
    }
}