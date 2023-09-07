package com.yolt.providers.stet.generic.http.signer.signature;

import com.yolt.providers.stet.generic.domain.SignatureData;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface SignatureStrategy {

    List<String> getHeadersToSign();

    String getSignature(HttpHeaders headers, SignatureData signatureData);
}
