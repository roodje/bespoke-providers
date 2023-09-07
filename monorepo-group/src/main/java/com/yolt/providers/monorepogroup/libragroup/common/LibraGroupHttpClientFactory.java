package com.yolt.providers.monorepogroup.libragroup.common;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.LibraGroupAuthenticationHttpClient;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.LibraGroupDataHttpClient;

public interface LibraGroupHttpClientFactory {
    LibraGroupAuthenticationHttpClient buildAuthorizationHttpClient(RestTemplateManager restTemplateManager);

    LibraGroupDataHttpClient buildDataHttpClient(RestTemplateManager restTemplateManager);
}
