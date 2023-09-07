package com.yolt.providers.stet.generic.http.signer.signature;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.NumericDate;
import org.springframework.http.HttpHeaders;

import java.util.*;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.DIGEST;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.X_REQUEST_ID;
import static org.springframework.http.HttpHeaders.HOST;

@Slf4j
@RequiredArgsConstructor
public class EnhancedCavageSignatureStrategy implements SignatureStrategy {

    private final SignatureAlgorithm signatureAlgorithm;

    @Override
    public List<String> getHeadersToSign() {
        return new LinkedList<>(Arrays.asList(HOST, X_REQUEST_ID, DIGEST));
    }

    @Override
    public String getSignature(HttpHeaders headers, SignatureData signatureData) {
        HttpHeaders httpHeaders = prepareSignatureHeaders(headers, signatureData.getHost());
        Map<String, String> signatureHeaders = new LinkedHashMap<>();
        getHeadersToSign().forEach(it -> {
            String headerValue = httpHeaders.getFirst(it);
            if (StringUtils.isEmpty(headerValue)) {
                throw new IllegalStateException("Missing HTTP header " + it + " required for Signature generation");
            }
            signatureHeaders.put(it, headerValue);
        });
        return signHeaders(signatureData, signatureHeaders);
    }

    protected HttpHeaders prepareSignatureHeaders(HttpHeaders headers, String host) {
        HttpHeaders signatureHeaders = new HttpHeaders();
        signatureHeaders.set(HOST, host);
        signatureHeaders.addAll(headers);
        return signatureHeaders;
    }

    protected String signHeaders(SignatureData signatureData, @NonNull final Map<String, String> headers) {
        List<String> headersKeys = new LinkedList<>();
        List<String> headersData = new LinkedList<>();

        headersKeys.add("(request-target)");
        String formattedHttpMethod = signatureData.getHttpMethod().name().toLowerCase();
        headersData.add(String.format("(request-target): %s %s", formattedHttpMethod, signatureData.getEndpoint()));

        headersKeys.add("(created)");
        NumericDate created = NumericDate.now();
        headersData.add(String.format("(created): %d", created.getValue()));

        headersKeys.add("(expires)");
        NumericDate expires = calculateExpiration(created);
        headersData.add(String.format("(expires): %d", expires.getValue()));

        headers.forEach((key, value) -> {
            headersKeys.add(key.toLowerCase());
            headersData.add(key.toLowerCase() + ": " + value);
        });
        String payload = String.join("\n", headersData);

        Signer signer = signatureData.getSigner();
        String signature = signer.sign(payload.getBytes(), signatureData.getSigningKeyId(), signatureAlgorithm);
        return getFormattedSignature(signatureData.getHeaderKeyId(), headersKeys, created, expires, signature);
    }

    private NumericDate calculateExpiration(NumericDate created) {
        NumericDate expires = NumericDate.fromSeconds(created.getValue());
        expires.addSeconds(60);
        return expires;
    }

    private String getFormattedSignature(@NonNull String headerKeyId, List<String> headersKeys, NumericDate created, NumericDate expires, String signature) {
        return String.format("keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",(created)=%d,(expires)=%d,signature=\"%s\"",
                headerKeyId, signatureAlgorithm.getHttpSignatureAlgorithm(), String.join(" ", headersKeys), created.getValue(), expires.getValue(), signature);
    }
}
