package com.yolt.providers.ing.common.pec;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.pec.initiate.DefaultInitiatePaymentPreExecutionResult;
import com.yolt.providers.ing.common.pec.submit.DefaultSubmitPaymentPreExecutionResult;
import com.yolt.providers.ing.common.service.IngSigningUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultCommonHttpHeadersProvider {

    public static final String PSU_IP_ADDRESS_HEADER_NAME = "PSU-IP-Address";
    public static final String DATE_HEADER_NAME = "Date";
    public static final String DIGEST_HEADER_NAME = "Digest";
    public static final String SIGNATURE_HEADER_NAME = "Signature";
    public static final String X_REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String TIME_FORMAT = "E, dd MMM yyyy HH:mm:ss z";

    public static final DateTimeFormatter ING_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT)
            .withZone(ZoneId.of("GMT")).withLocale(Locale.ENGLISH);

    private final IngSigningUtil ingSigningUtil;
    private final Clock clock;
    private final Supplier<String> externalTracingIdSupplier;

    public HttpHeaders provideHttpHeaders(final DefaultInitiatePaymentPreExecutionResult preExecutionResult,
                                          final byte[] requestBody,
                                          final HttpMethod httpMethod,
                                          final String endpointPath) {
        IngClientAccessMeans accessMeans = preExecutionResult.getClientAccessMeans();
        IngAuthenticationMeans authMeans = preExecutionResult.getAuthenticationMeans();
        Signer signer = preExecutionResult.getSigner();
        String psuIpAddress = preExecutionResult.getPsuIpAddress();

        return provideHttpHeaders(accessMeans, authMeans, requestBody, httpMethod, endpointPath, signer, psuIpAddress);
    }

    public HttpHeaders provideHttpHeaders(final DefaultSubmitPaymentPreExecutionResult preExecutionResult,
                                          final byte[] requestBody,
                                          final HttpMethod httpMethod,
                                          final String endpointPath) {
        IngClientAccessMeans accessMeans = preExecutionResult.getClientAccessMeans();
        IngAuthenticationMeans authMeans = preExecutionResult.getAuthenticationMeans();
        Signer signer = preExecutionResult.getSigner();
        String psuIpAddress = preExecutionResult.getPsuIpAddress();

        return provideHttpHeaders(accessMeans, authMeans, requestBody, httpMethod, endpointPath, signer, psuIpAddress);
    }

    private HttpHeaders provideHttpHeaders(final IngClientAccessMeans accessMeans,
                                           final IngAuthenticationMeans authMeans,
                                           final byte[] requestBody,
                                           final HttpMethod httpMethod,
                                           final String endpointPath,
                                           final Signer signer,
                                           final String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessMeans.getAccessToken());
        headers.add(DATE_HEADER_NAME, ING_DATETIME_FORMATTER.format(Instant.now(clock)));
        headers.add(DIGEST_HEADER_NAME, ingSigningUtil.getDigest(requestBody));
        headers.add(X_REQUEST_ID_HEADER_NAME, externalTracingIdSupplier.get());
        headers.add(SIGNATURE_HEADER_NAME, ingSigningUtil.getSignature(
                httpMethod,
                endpointPath,
                headers,
                accessMeans.getClientId(),
                authMeans.getSigningKeyId(),
                signer));
        if (StringUtils.isNotBlank(psuIpAddress)) {
            headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        }

        return headers;
    }
}
