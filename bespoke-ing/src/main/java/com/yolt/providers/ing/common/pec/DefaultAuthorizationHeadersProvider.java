package com.yolt.providers.ing.common.pec;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.config.IngProperties;
import com.yolt.providers.ing.common.service.IngSigningUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RequiredArgsConstructor
public class DefaultAuthorizationHeadersProvider {

    public static final String DIGEST_HEADER_NAME = "Digest";
    public static final String TIME_FORMAT = "E, dd MMM yyyy HH:mm:ss z";
    public static final DateTimeFormatter ING_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT)
            .withZone(ZoneId.of("GMT")).withLocale(Locale.ENGLISH);
    public static final String TPP_SIGNATURE_CERTIFICATE_HEADER_NAME = "TPP-Signature-Certificate";
    public static final String SIGNATURE = "Signature";

    private final IngSigningUtil ingSigningUtil;
    private final IngProperties properties;
    private final Clock clock;

    HttpHeaders provideHttpHeaders(final MultiValueMap<String, Object> requestPayload,
                                   final IngAuthenticationMeans authenticationMeans,
                                   final Signer signer) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.DATE, ING_DATETIME_FORMATTER.format(clock.instant()));
        headers.add(DIGEST_HEADER_NAME, ingSigningUtil.getDigest(requestPayload));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String signature = ingSigningUtil.getSignature(
                HttpMethod.POST,
                properties.getOAuthTokenEndpoint(),
                headers,
                authenticationMeans.getSigningCertificateSerialNumber(),
                authenticationMeans.getSigningKeyId(),
                signer);
        headers.add(HttpHeaders.AUTHORIZATION, SIGNATURE + " " + signature);
        headers.add(TPP_SIGNATURE_CERTIFICATE_HEADER_NAME, authenticationMeans.getSigningCertificatePemFormat());
        return headers;
    }
}