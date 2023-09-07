package com.yolt.providers.stet.generic.http.client;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;

public interface HttpClientFactory {

    HttpClient createHttpClient(RestTemplateManager restTemplateManager,
                                DefaultAuthenticationMeans authMeans,
                                String baseUrl,
                                String providerDisplayName);
}
