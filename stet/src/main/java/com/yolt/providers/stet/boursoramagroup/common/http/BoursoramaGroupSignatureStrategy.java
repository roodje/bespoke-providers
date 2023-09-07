package com.yolt.providers.stet.boursoramagroup.common.http;

import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.signer.signature.SignatureStrategy;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoursoramaGroupSignatureStrategy implements SignatureStrategy {

    private static final String HEADER_DIGEST = "digest";
    private static final String HEADER_X_REQUEST_ID = "x-request-id";
    private static final String HEADER_PSU_IP_ADDRESS = "psu-ip-address";
    private static final List<String> PSU_HEADERS_TO_SIGN = Arrays.asList(HEADER_DIGEST, HEADER_PSU_IP_ADDRESS, HEADER_X_REQUEST_ID);

    private final SignatureAlgorithm signatureAlgorithm;

    public BoursoramaGroupSignatureStrategy(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    @Override
    public List<String> getHeadersToSign() {
        return PSU_HEADERS_TO_SIGN;
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
        BoursoramaSigner httpSigner = new BoursoramaSigner(signatureData.getSigner(), signatureData.getSigningKeyId(), signatureAlgorithm);
        return httpSigner.signHeaders(headersToSign, signatureData.getHeaderKeyId(), signatureData.getHttpMethod().toString(), signatureData.getEndpoint());
    }
}
