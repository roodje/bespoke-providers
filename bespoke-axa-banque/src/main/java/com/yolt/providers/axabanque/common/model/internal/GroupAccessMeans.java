package com.yolt.providers.axabanque.common.model.internal;

import lombok.Value;
import lombok.With;

@Value
public class GroupAccessMeans {
    String baseRedirectUri;
    GroupProviderState providerState;
    @With
    AccessToken accessToken;
}
