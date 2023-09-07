package com.yolt.providers.brdgroup.common.http;

import com.yolt.providers.brdgroup.common.BrdGroupAuthenticationMeans;
import com.yolt.providers.common.cryptography.RestTemplateManager;

public interface BrdGroupHttpClientFactory {

    BrdGroupHttpClient createHttpClient(BrdGroupAuthenticationMeans authMeans,
                                        RestTemplateManager restTemplateManager,
                                        String providerDisplayName);
}
