package com.yolt.providers.rabobank.pis.pec;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.rabobank.SigningUtil;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RabobankPisHeadersSigner {

    private static final List<String> PIS_HEADERS_TO_SIGN = Arrays.asList("digest", "date", "tpp-redirect-uri", "tpp-nok-redirect-uri", "x-request-id");
    private static final String TPP_SIGNATURE_CERTIFICATE_NAME = "tpp-signature-certificate";
    private static final String DIGEST_NAME = "digest";
    private static final String SIGNATURE_NAME = "signature";

    public HttpHeaders signHeaders(HttpHeaders headers, byte[] body, Signer signer, UUID signingKeyId, X509Certificate signingCertificate) throws CertificateEncodingException {
        String encodedCertificate;
        encodedCertificate = Base64.toBase64String(signingCertificate.getEncoded());
        headers.add(TPP_SIGNATURE_CERTIFICATE_NAME, encodedCertificate);
        headers.add(DIGEST_NAME, SigningUtil.getDigest(body));
        headers.add(SIGNATURE_NAME, SigningUtil.getSigningString(signer, headers, signingCertificate.getSerialNumber().toString(), signingKeyId, PIS_HEADERS_TO_SIGN));
        return headers;
    }
}
