package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;

public interface CecGroupHttpClientFactory {

    CecGroupHttpClient createHttpClient(CecGroupAuthenticationMeans authMeans,
                                        RestTemplateManager restTemplateManager,
                                        String providerDisplayName);
}
