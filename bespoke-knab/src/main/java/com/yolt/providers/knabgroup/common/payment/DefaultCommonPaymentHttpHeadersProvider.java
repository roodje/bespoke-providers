package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.knabgroup.common.auth.KnabSigningService;
import com.yolt.providers.knabgroup.common.auth.SignatureData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultCommonPaymentHttpHeadersProvider {

    public static final String PSU_IP_ADDRESS_HEADER_NAME = "PSU-IP-Address";
    public static final String DIGEST_HEADER_NAME = "Digest";
    public static final String SIGNATURE_HEADER_NAME = "Signature";
    public static final String X_REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    private static final List<String> HEADERS_IN_SIGNATURE = List.of(DIGEST_HEADER_NAME, X_REQUEST_ID_HEADER_NAME);


    private final Supplier<String> externalTracingIdSupplier;
    private final KnabSigningService signingService;

    public HttpHeaders provideHttpHeaders(final String accessToken,
                                          final SignatureData signatureData,
                                          final byte[] requestBody,
                                          final String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.add(DIGEST_HEADER_NAME, signingService.calculateDigest(requestBody));
        headers.add(X_REQUEST_ID_HEADER_NAME, externalTracingIdSupplier.get());
        headers.add(TPP_SIGNATURE_CERTIFICATE, signatureData.getSigningCertificateInBase64());
        headers.add(SIGNATURE_HEADER_NAME, signingService.calculateSignature(
                headers,
                signatureData,
                HEADERS_IN_SIGNATURE
        ));
        if (StringUtils.isNotBlank(psuIpAddress)) {
            headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        }
        return headers;
    }
}
