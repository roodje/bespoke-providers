package com.yolt.providers.gruppocedacri.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriAuthenticationMeans;

public interface GruppoCedacriHttpClientFactory {

    GruppoCedacriHttpClient createHttpClient(GruppoCedacriAuthenticationMeans authenticationMeans,
                                             RestTemplateManager restTemplateManager,
                                             String provider);
}
