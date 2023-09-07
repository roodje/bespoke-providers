package com.yolt.providers.stet.societegeneralegroup.common.http.signer.signature;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.signer.signature.SignatureStrategy;
import com.yolt.providers.stet.societegeneralegroup.common.http.signer.SocieteGeneraleHttpSigner;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SocieteGeneraleGroupSignatureStrategy implements SignatureStrategy {

    private static final String HEADER_DIGEST = "digest";
    private static final String HEADER_X_REQUEST_ID = "x-request-id";
    private static final String CLIENT_ID = "client-id";
    private static final List<String> HEADERS_TO_SIGN = Arrays.asList(HEADER_DIGEST, CLIENT_ID, HEADER_X_REQUEST_ID);

    private final SignatureAlgorithm signatureAlgorithm;
    private final DefaultProperties properties;

    public SocieteGeneraleGroupSignatureStrategy(SignatureAlgorithm signatureAlgorithm, DefaultProperties properties) {
        this.signatureAlgorithm = signatureAlgorithm;
        this.properties = properties;
    }

    @Override
    public List<String> getHeadersToSign() {
        return HEADERS_TO_SIGN;
    }

    public Map<String, String> getHeadersToSign(HttpHeaders headers) {
        final Map<String, String> headersToSign = headers.toSingleValueMap().entrySet().stream()
                .filter(entry -> getHeadersToSign().contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return headersToSign.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    @Override
    public String getSignature(HttpHeaders headers, SignatureData signatureData) {
        Map<String, String> headersToSign = getHeadersToSign(headers);
        SocieteGeneraleHttpSigner httpSigner = new SocieteGeneraleHttpSigner(signatureData.getSigner(), signatureData.getSigningKeyId(), signatureAlgorithm, properties);
        return httpSigner.signHeaders(headersToSign, signatureData.getSigningCertificate());
    }
}
