package com.yolt.providers.axabanque.common.auth.http.clientproducer;

import com.yolt.providers.axabanque.common.auth.http.client.AuthorizationHttpClient;
import com.yolt.providers.axabanque.common.fetchdata.http.client.FetchDataHttpClient;
import com.yolt.providers.common.cryptography.RestTemplateManager;

import java.security.cert.X509Certificate;
import java.util.UUID;

public interface HttpClientProducer {
    AuthorizationHttpClient getAuthenticationHttpClient(UUID transportKeyId, X509Certificate tlsCertificate, RestTemplateManager restTemplateManager);

    FetchDataHttpClient getFetchDataHttpClient(UUID transportKeyId, X509Certificate tlsCertificate, RestTemplateManager restTemplateManager);
}
