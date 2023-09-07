package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;

public interface RaiffeisenAtGroupHttpClientProducer {

    RaiffeisenAtGroupHttpClient createHttpClient(final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final RestTemplateManager restTemplateManager);
}
