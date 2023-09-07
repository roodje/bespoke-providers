package com.yolt.providers.stet.bpcegroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.signer.signature.CavageSignatureStrategy;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.DIGEST;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.X_REQUEST_ID;
import static org.springframework.http.HttpHeaders.DATE;


public class BpceGroupSignatureStrategy extends CavageSignatureStrategy {

    private final SignatureAlgorithm signatureAlgorithm;

    public BpceGroupSignatureStrategy(SignatureAlgorithm signatureAlgorithm) {
        super(signatureAlgorithm);
        this.signatureAlgorithm = signatureAlgorithm;
    }

    @Override
    public List<String> getHeadersToSign() {
        return new LinkedList<>(Arrays.asList(DATE, DIGEST, X_REQUEST_ID));
    }

    @Override
    public String getSignature(HttpHeaders headers, SignatureData signatureData) {
        return getSignatureHeaderValue(signatureData.getEndpoint(), headers, signatureData);

    }

    private String getSignatureHeaderValue(String path, HttpHeaders headers, SignatureData signatureData) {
        String messageToSign = getMessageToSign(path, HttpMethod.POST, headers);

        Signer signer = signatureData.getSigner();
        String signedMessage = signer.sign(messageToSign.getBytes(), signatureData.getSigningKeyId(), signatureAlgorithm);

        return getSignature(signatureData.getHeaderKeyId(), headers, signedMessage);
    }

    private String getMessageToSign(String path, HttpMethod method, HttpHeaders headers) {
        return String.format("(request-target): %s %s\n%s",
                method.toString().toLowerCase(),
                path,
                getFormattedOrderedHttpHeadersKeys(headers));
    }

    private String getFormattedOrderedHttpHeadersKeys(HttpHeaders headers) {
        return getOrderedHttpHeadersKeys(headers).stream()
                .map(header -> header + ": " + getFormattedHttpHeaderValues(headers.get(header)))
                .collect(Collectors.joining("\n"));
    }

    private String getFormattedHttpHeaderValues(List<String> headerValues) {
        return String.join(", ", headerValues);
    }

    private String getSignature(String keyId, HttpHeaders headers, String signature) {
        return String.format("keyId=\"%s\",algorithm=\"%s\",headers=\"(request-target) %s\",signature=\"%s\"",
                keyId,
                signatureAlgorithm.getHttpSignatureAlgorithm(),
                String.join(" ", getOrderedHttpHeadersKeys(headers)),
                signature);
    }

    private List<String> getOrderedHttpHeadersKeys(HttpHeaders headers) {
        return headers.toSingleValueMap().keySet().stream()
                .filter(header -> getHeadersToSign().contains(header))
                .map(String::toLowerCase)
                .sorted()
                .collect(Collectors.toList());
    }
}
