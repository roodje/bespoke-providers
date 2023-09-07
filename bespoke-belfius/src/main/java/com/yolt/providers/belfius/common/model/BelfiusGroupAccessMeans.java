package com.yolt.providers.belfius.common.model;

import lombok.Data;

@Data
public class BelfiusGroupAccessMeans {
    private final BelfiusGroupAccessToken accessToken;
    private final String language;
    private final String redirectUrl;
}
