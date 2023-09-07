package com.yolt.providers.stet.generic.http.signer.signature;

import com.yolt.providers.common.cryptography.CavageHttpSigning;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.DIGEST;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.X_REQUEST_ID;
import static org.springframework.http.HttpHeaders.*;

@RequiredArgsConstructor
public class CavageSignatureStrategy implements SignatureStrategy {

    private final SignatureAlgorithm signatureAlgorithm;

    @Override
    public List<String> getHeadersToSign() {
        return new LinkedList<>(Arrays.asList(ACCEPT, HOST, DATE, CONTENT_TYPE, X_REQUEST_ID, DIGEST, AUTHORIZATION));
    }

    @Override
    public String getSignature(HttpHeaders headers, SignatureData signatureData) {
        final Map<String, String> headersToSign = headers.toSingleValueMap().entrySet().stream()
                .filter(entry -> getHeadersToSign().contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        CavageHttpSigning httpSigner = new CavageHttpSigning(signatureData.getSigner(), signatureData.getSigningKeyId(), signatureAlgorithm);
        return httpSigner.signHeaders(headersToSign, signatureData.getHeaderKeyId(), signatureData.getHttpMethod().toString(), signatureData.getEndpoint());
    }
}
