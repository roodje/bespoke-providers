package com.yolt.providers.monorepogroup.olbgroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeans;

public interface OlbGroupHttpClientFactory {
    OlbGroupHttpClient createHttpClient(OlbGroupAuthenticationMeans authMeans,
                                        RestTemplateManager restTemplateManager,
                                        String providerDisplayName);
}
