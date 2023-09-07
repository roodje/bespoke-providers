package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor
public class CecGroupHttpHeadersProducerV1 implements CecGroupHttpHeadersProducer {

    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    private static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    private static final String TPP_REDIRECT_URI_HEADER = "tpp-redirect-uri";
    private static final String TPP_NOK_REDIRECT_URI_HEADER = "tpp-nok-redirect-uri";
    private static final String X_IBM_CLIENT_ID_HEADER = "X-IBM-Client-Id";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER = "tpp-signature-certificate";
    private static final String DIGEST_HEADER = "digest";
    private static final String SIGNATURE_HEADER = "signature";
    private static final String TOKEN_HEADER = "oauth-1";
    private static final String CONSENT_ID_HEADER = "consent-id";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER_VALUE_TEMPLATE = "-----BEGIN CERTIFICATE-----%s-----END CERTIFICATE-----";

    private static final List<String> HEADERS_TO_SIGN = List.of(DIGEST_HEADER, PSU_IP_ADDRESS_HEADER, X_REQUEST_ID_HEADER);

    @SneakyThrows(CertificateEncodingException.class)
    @Override
    public HttpHeaders createConsentHeaders(String psuIpAddress,
                                            String redirectUri,
                                            String state,
                                            CecGroupAuthenticationMeans authMeans,
                                            byte[] body,
                                            Signer signer) {
        X509Certificate signingCertificate = authMeans.getSigningCertificate();

        HttpHeaders headers = createCommonHeaders();
        headers.add(X_IBM_CLIENT_ID_HEADER, authMeans.getClientId());
        headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        headers.add(TPP_REDIRECT_PREFERRED_HEADER, "true");
        headers.add(TPP_REDIRECT_URI_HEADER, redirectUri + "?state=" + state);
        headers.add(TPP_NOK_REDIRECT_URI_HEADER, redirectUri + "?state=" + state + "&error=true");
        headers.add(TPP_SIGNATURE_CERTIFICATE_HEADER, String.format(TPP_SIGNATURE_CERTIFICATE_HEADER_VALUE_TEMPLATE,
                Base64.toBase64String(signingCertificate.getEncoded())));
        headers.add(DIGEST_HEADER, SigningUtil.getDigest(body));
        headers.add(SIGNATURE_HEADER, SigningUtil.getSigningString(signer, headers, signingCertificate.getSerialNumber().toString(),
                UUID.fromString(authMeans.getSigningKeyId()), HEADERS_TO_SIGN));
        return headers;
    }

    @Override
    public HttpHeaders tokenHeaders(String clientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.add(X_IBM_CLIENT_ID_HEADER, clientId);
        return headers;
    }

    @SneakyThrows(CertificateEncodingException.class)
    @Override
    public HttpHeaders fetchDataHeaders(String psuIpAddress,
                                        CecGroupAuthenticationMeans authMeans,
                                        CecGroupAccessMeans cecGroupAccessMeans,
                                        Signer signer) {
        X509Certificate signingCertificate = authMeans.getSigningCertificate();
        HttpHeaders headers = createCommonHeaders();
        if (StringUtils.hasText(psuIpAddress)) {
            headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        headers.add(TOKEN_HEADER, cecGroupAccessMeans.getAccessToken());
        headers.add(CONSENT_ID_HEADER, cecGroupAccessMeans.getConsentId());
        headers.add(X_IBM_CLIENT_ID_HEADER, authMeans.getClientId());
        headers.add(TPP_SIGNATURE_CERTIFICATE_HEADER, Base64.toBase64String(signingCertificate.getEncoded()));
        headers.add(DIGEST_HEADER, SigningUtil.getDigest(new byte[0]));
        headers.add(SIGNATURE_HEADER, SigningUtil.getSigningString(signer, headers, signingCertificate.getSerialNumber().toString(),
                UUID.fromString(authMeans.getSigningKeyId()), HEADERS_TO_SIGN));
        return headers;
    }

    private HttpHeaders createCommonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }
}
