package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;

public interface CheBancaGroupHttpClientProducer {

    CheBancaGroupHttpClient createHttpClient(final CheBancaGroupAuthenticationMeans authenticationMeans, final RestTemplateManager restTemplateManager);
}
