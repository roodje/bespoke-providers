package com.yolt.providers.axabanque.common.auth.mapper.access;

import com.yolt.providers.axabanque.common.model.internal.AccessToken;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.axabanque.common.model.internal.GroupProviderState;

public class DefaultAccessMeansMapper implements AccessMeansMapper {

    @Override
    public GroupAccessMeans mapToAccessMeans(String baseRedirectUri, GroupProviderState providerState, AccessToken token) {
        return new GroupAccessMeans(
                baseRedirectUri,
                providerState,
                token);
    }
}
