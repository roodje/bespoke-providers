package com.yolt.providers.argentagroup.common.service.consent;

import com.yolt.providers.argentagroup.common.http.JsonBodyDigestProducer;
import com.yolt.providers.argentagroup.common.http.SignatureData;
import com.yolt.providers.argentagroup.common.http.SignatureProducer;
import com.yolt.providers.argentagroup.common.http.TppSignatureCertificateHeaderProducer;
import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DeleteConsentHttpHeadersProvider {

    private static final String X_REQUEST_ID_HEADER_NAME = "X-Request-Id";
    private static final String DIGEST_HEADER_NAME = "digest";
    private static final String SIGNATURE_HEADER_NAME = "signature";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER_NAME = "tpp-signature-certificate";
    private static final String PSU_IP_ADDRESS_HEADER_NAME = "psu-ip-address";
    private static final String API_KEY_HEADER_NAME = "apiKey";


    private final Supplier<String> externalTracingIdProvider;
    private final JsonBodyDigestProducer digestProducer;
    private final SignatureProducer signatureProducer;
    private final TppSignatureCertificateHeaderProducer certificateHeaderProducer;

    public HttpHeaders provideRequestHeaders(final UrlOnUserSiteDeleteRequest request,
                                             final DefaultAuthenticationMeans authenticationMeans,
                                             final AccessMeans accessMeans) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessMeans.getAccessToken());
        headers.set(X_REQUEST_ID_HEADER_NAME, externalTracingIdProvider.get());
        headers.set(DIGEST_HEADER_NAME, digestProducer.calculateSHA256Digest(new byte[0]));
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER_NAME, certificateHeaderProducer.getTppSignatureCertificateHeaderValue(authenticationMeans.getSigningCertificate()));
        headers.set(PSU_IP_ADDRESS_HEADER_NAME, request.getPsuIpAddress());
        headers.set(API_KEY_HEADER_NAME, authenticationMeans.getApiKey());

        SignatureData signatureData = new SignatureData(
                authenticationMeans.getSigningKeyId(),
                request.getSigner(),
                authenticationMeans.getSigningCertificate(),
                headers,
                List.of(X_REQUEST_ID_HEADER_NAME, DIGEST_HEADER_NAME),
                SignatureAlgorithm.SHA512_WITH_RSA
        );
        headers.set(SIGNATURE_HEADER_NAME, signatureProducer.calculateSignature(signatureData));

        return headers;
    }
}
