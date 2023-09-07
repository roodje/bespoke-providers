package com.yolt.providers.monorepogroup.atruviagroup.common.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor
public class AtruviaGroupHttpHeadersProducer {

    static final String X_REQUEST_ID_HEADER = "X-Request-ID";
    private static final String PSU_IP_ADDRESS_HEADER = "PSU-IP-Address";
    private static final String PSU_ID_HEADER = "PSU-ID";
    private static final String CONSENT_ID_HEADER = "Consent-ID";
    private static final String DIGEST_HEADER = "Digest";
    private static final String SIGNATURE_HEADER = "Signature";
    private static final String SIGNATURE_CERTIFICATE_HEADER = "TPP-Signature-Certificate";

    private final AtruviaGroupSigningUtil signingUtil;
    private final ObjectMapper objectMapper;

    public HttpHeaders createFetchDataHeaders(String consentId,
                                              String psuIpAddress,
                                              X509Certificate signingCertificate,
                                              UUID signingKeyId,
                                              Signer signer) {
        HttpHeaders headers = createBasicHeadersWithSignature(psuIpAddress, signingCertificate, signingKeyId, signer, new byte[0]);
        headers.set(CONSENT_ID_HEADER, consentId);
        return headers;
    }

    public HttpHeaders createAuthorizationHeaders(String psuId,
                                                  String psuIpAddress,
                                                  X509Certificate signingCertificate,
                                                  UUID signingKeyId,
                                                  Signer signer,
                                                  Object body) {
        try {
            var jsonBody = objectMapper.writeValueAsString(body);
            var bodyBytes = jsonBody.getBytes();
            return createAuthorizationHeaders(null, psuId, psuIpAddress, signingCertificate, signingKeyId, signer, bodyBytes);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize body to json");
        }

    }

    public HttpHeaders createAuthorizationHeaders(String consentId,
                                                  String psuId,
                                                  String psuIpAddress,
                                                  X509Certificate signingCertificate,
                                                  UUID signingKeyId,
                                                  Signer signer,
                                                  byte[] body) {
        var multivalueMap = new LinkedMultiValueMap<String, String>(2);
        Optional.ofNullable(consentId)
                .ifPresent((c) -> multivalueMap.add(CONSENT_ID_HEADER, c));
        Optional.ofNullable(psuId)
                .ifPresent((p) -> multivalueMap.add(PSU_ID_HEADER, p));
        return createBasicHeadersWithSignature(psuIpAddress, signingCertificate, signingKeyId, multivalueMap, signer, body);
    }

    private HttpHeaders createBasicHeadersWithSignature(String psuIpAddress,
                                                        X509Certificate signingCertificate,
                                                        UUID signingKeyId,
                                                        Signer signer,
                                                        byte[] body) {
        return createBasicHeadersWithSignature(psuIpAddress, signingCertificate, signingKeyId, new LinkedMultiValueMap<>(0), signer, body);
    }

    @SneakyThrows(CertificateEncodingException.class)
    private HttpHeaders createBasicHeadersWithSignature(String psuIpAddress,
                                                        X509Certificate signingCertificate,
                                                        UUID signingKeyId,
                                                        MultiValueMap<String, String> additionalHeaders,
                                                        Signer signer,
                                                        byte[] body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }

        headers.addAll(additionalHeaders);
        headers.add(DIGEST_HEADER, signingUtil.getDigest(body));
        headers.add(SIGNATURE_HEADER, signingUtil.getSignature(headers, signingCertificate, signingKeyId, signer));
        headers.add(SIGNATURE_CERTIFICATE_HEADER, Base64.toBase64String(signingCertificate.getEncoded()));
        return headers;
    }
}
