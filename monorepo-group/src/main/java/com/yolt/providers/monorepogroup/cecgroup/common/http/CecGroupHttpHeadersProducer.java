package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import org.springframework.http.HttpHeaders;

public interface CecGroupHttpHeadersProducer {

    HttpHeaders createConsentHeaders(String psuIpAddress,
                                     String redirectUri,
                                     String state,
                                     CecGroupAuthenticationMeans authMeans,
                                     byte[] body,
                                     Signer signer);

    HttpHeaders tokenHeaders(String clientId);

    HttpHeaders fetchDataHeaders(String psuIpAddress,
                                 CecGroupAuthenticationMeans authenticationMeans,
                                 CecGroupAccessMeans cecGroupAccessMeans,
                                 Signer signer);
}
