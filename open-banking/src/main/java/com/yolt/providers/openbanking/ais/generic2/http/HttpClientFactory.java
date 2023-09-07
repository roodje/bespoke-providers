package com.yolt.providers.openbanking.ais.generic2.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;

public interface HttpClientFactory {

    HttpClient createHttpClient(final RestTemplateManager restTemplateManager,
                                final DefaultAuthMeans authenticationMeans,
                                final String providerDisplayName);
}
