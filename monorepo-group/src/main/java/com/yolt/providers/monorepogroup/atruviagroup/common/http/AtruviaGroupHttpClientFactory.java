package com.yolt.providers.monorepogroup.atruviagroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;

public interface AtruviaGroupHttpClientFactory {

    AtruviaGroupHttpClient createHttpClient(RestTemplateManager restTemplateManager,
                                            String providerDisplayName,
                                            String baseUrl);
}
