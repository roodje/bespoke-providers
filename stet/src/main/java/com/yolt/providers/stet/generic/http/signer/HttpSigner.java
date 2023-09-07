package com.yolt.providers.stet.generic.http.signer;

import com.yolt.providers.stet.generic.domain.SignatureData;
import org.springframework.http.HttpHeaders;

public interface HttpSigner {

    String getDigest(Object requestBody);

    String getSignature(HttpHeaders headers, SignatureData signatureData);
}
