package com.yolt.providers.openbanking.ais.hsbcgroup.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsbcGroupAccessMeansV2 extends AccessMeans {

    //TODO serialize and deserialize methods need refactoring in separate task
    private Instant created = Instant.ofEpochMilli(0L);

    private UUID userId;

    private String accessToken;

    private String refreshToken;

    private Date expireTime;

    private Date updated;

    @JsonProperty("redirecturi")
    private String redirectUri;
}
