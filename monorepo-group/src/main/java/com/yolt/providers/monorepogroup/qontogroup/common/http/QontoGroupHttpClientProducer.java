package com.yolt.providers.monorepogroup.qontogroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;

public interface QontoGroupHttpClientProducer {

    QontoGroupHttpClient createHttpClient(final QontoGroupAuthenticationMeans authenticationMeans, final RestTemplateManager restTemplateManager);
}
