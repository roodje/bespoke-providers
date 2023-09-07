package com.yolt.providers.stet.generic.http.signer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.signer.signature.SignatureStrategy;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
public class DefaultHttpSigner implements HttpSigner {

    protected final SignatureStrategy signatureStrategy;
    protected final ObjectMapper objectMapper;
    protected final DigestAlgorithm digestAlgorithm;

    @Override
    public String getDigest(Object requestBody) {
        try {
            String algorithm = digestAlgorithm.getAlgorithm();
            byte[] digest = MessageDigest.getInstance(algorithm).digest(getSerializedBody(requestBody));
            return algorithm + "=" + Base64.toBase64String(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm not configured properly for digest calculation", e);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Object mapper failed during serialization of request body");
        }
    }

    private byte[] getSerializedBody(Object requestBody) throws JsonProcessingException {
        if (requestBody == null) {
            return new byte[0];
        }
        if (requestBody instanceof byte[]) {
            return (byte[]) requestBody;
        }
        return objectMapper.writeValueAsString(requestBody).getBytes(UTF_8);
    }

    @Override
    public String getSignature(HttpHeaders headers, SignatureData signatureData) {
        return signatureStrategy.getSignature(headers, signatureData);
    }
}
