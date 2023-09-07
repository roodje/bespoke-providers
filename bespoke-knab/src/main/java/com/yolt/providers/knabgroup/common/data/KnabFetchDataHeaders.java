package com.yolt.providers.knabgroup.common.data;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.knabgroup.common.auth.KnabSigningService;
import com.yolt.providers.knabgroup.common.auth.SignatureData;
import com.yolt.providers.knabgroup.common.dto.internal.KnabAccessMeans;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

class KnabFetchDataHeaders extends HttpHeaders {

    private static final DateTimeFormatter KNAB_DATETIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    private static final String X_REQUEST_ID = "X-Request-ID";
    private static final String DIGEST = "Digest";
    private static final String SIGNATURE = "Signature";
    private static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String CONSENT_ID = "Consent-ID";

    KnabFetchDataHeaders(KnabSigningService signingService, Instant currentTime, KnabAccessMeans accessMeans, SignatureData signatureData, Optional<String> psuIpAddress) throws TokenInvalidException {
        this.setBearerAuth(accessMeans.getAccessToken());
        this.add(DATE, KNAB_DATETIME_FORMATTER.format(currentTime));
        this.add(X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        this.add(DIGEST, signingService.calculateDigest(new LinkedMultiValueMap()));
        this.add(TPP_SIGNATURE_CERTIFICATE, signatureData.getSigningCertificateInBase64());
        this.add(CONSENT_ID, retrieveConsentIdFromScope(accessMeans.getScope()));
        psuIpAddress.ifPresent(ipAddress -> this.add(PSU_IP_ADDRESS, ipAddress));

        this.add(SIGNATURE, signingService.calculateSignature(this, signatureData, Arrays.asList(DIGEST, X_REQUEST_ID, DATE)));
    }

    private String retrieveConsentIdFromScope(final String scope) throws TokenInvalidException {
        Optional<String> singleScopeWithConsentId = Arrays.stream(scope.split(" ")).filter(value -> value.contains("AIS")).findFirst();
        if (!singleScopeWithConsentId.isPresent()) {
            throw new TokenInvalidException("Missing consentId in scope of user accessToken");
        }
        return singleScopeWithConsentId.get().split(":")[1];
    }
}
