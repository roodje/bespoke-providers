package com.yolt.providers.monorepogroup.qontogroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.springframework.http.HttpHeaders;

import java.util.LinkedList;
import java.util.List;

public class QontoGroupHttpHeaders extends HttpHeaders {

    private static final String SHA_256_DIGEST_OF_EMPTY_BODY = "SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
    private static final String SIGNATURE_FORMAT = "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.SHA256_WITH_RSA;

    public void sign(final QontoGroupAuthenticationMeans.SigningData signingData, final Signer signer) {
        this.add("Digest", SHA_256_DIGEST_OF_EMPTY_BODY);
        List<String> headersKeys = new LinkedList<>();
        List<String> headersData = new LinkedList<>();
        this.toSingleValueMap().forEach((key, value) -> {
            headersKeys.add(key.toLowerCase());
            headersData.add(key.toLowerCase() + ": " + value);
        });
        String payload = String.join("\n", headersData);
        String signature = signer.sign(payload.getBytes(), signingData.signingKeyId(), SIGNATURE_ALGORITHM);
        String signatureString = String.format(SIGNATURE_FORMAT, signingData.certificateUrl(),
                SIGNATURE_ALGORITHM.getHttpSignatureAlgorithm(),
                String.join(" ", headersKeys),
                signature);
        this.add("Signature", signatureString);
    }
}
