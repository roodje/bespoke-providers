package com.yolt.providers.monorepogroup.handelsbankengroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeans;

public interface HandelsbankenGroupHttpClientFactory {

    HandelsbankenGroupHttpClient createHttpClient(HandelsbankenGroupAuthMeans authMeans,
                                                  RestTemplateManager restTemplateManager,
                                                  String provider);
}
