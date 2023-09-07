package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.monorepogroup.chebancagroup.common.CheBancaGroupProperties;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.SignatureDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DefaultCheBancaGroupHttpHeadersProducer implements CheBancaGroupHttpHeadersProducer {

    private final Clock clock;
    private final CheBancaGroupProperties properties;
    private static final String HEADER_DIGEST = "digest";
    private static final String HEADER_SIGNATURE = "signature";
    private static final String HEADER_TPP_REQUEST_ID = "TPP-Request-ID";
    private static final String HEADER_DATE = "Date";
    private static final String HEADER_HOST = "Host";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    @Override
    public HttpHeaders createAuthorizationHttpHeaders(final SignatureDTO signatureDTO, final Signer signer, final byte[] body) {
        HashMap<String, String> headersToSign = new HashMap<>();
        return createBasicHeaders(signatureDTO, signer, headersToSign);
    }

    @Override
    public HttpHeaders createGetTokenHttpHeaders(final SignatureDTO signatureDTO, final Signer signer, final byte[] body) {
        HashMap<String, String> headersToSign = new HashMap<>();
        headersToSign.put(HEADER_DIGEST, HttpHeaderDigest.createDigestHeaderValue(body));
        return createBasicHeaders(signatureDTO, signer, headersToSign);
    }

    @Override
    public HttpHeaders getFetchDataHeaders(final SignatureDTO signatureDTO, final Signer signer, final byte[] body, String clientAccessToken) {
        HashMap<String, String> headersToSign = new HashMap<>();
        return createBasicHeadersForFetching(signatureDTO, signer, headersToSign, clientAccessToken);
    }

    public HttpHeaders createBasicHeaders(final SignatureDTO signatureDTO,
                                          final Signer signer,
                                          final Map<String, String> headersToSign) {
        ChebancaGroupCavageHttpSigning httpSigning = new ChebancaGroupCavageHttpSigning(signer, signatureDTO.getSigningKid(), signatureDTO.getAlgorithm());
        headersToSign.put(HEADER_TPP_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        headersToSign.put(HEADER_DATE, FORMATTER.format(Instant.now(clock)));

        HttpHeaders headers = new HttpHeaders();
        headersToSign.forEach(headers::add);
        headers.setContentType(MediaType.valueOf("application/x-www-form-urlencoded;charset=UTF-8"));
        headers.add(HEADER_HOST, properties.getBaseUrl().replaceAll("https://", ""));
        headers.add(HEADER_SIGNATURE, httpSigning.signHeaders(headersToSign, signatureDTO.getKeyId(), signatureDTO.getMethod(), signatureDTO.getPath()));
        return headers;
    }

    public HttpHeaders createBasicHeadersForFetching(final SignatureDTO signatureDTO,
                                                     final Signer signer,
                                                     final Map<String, String> headersToSign,
                                                     final String clientAccessToken) {
        ChebancaGroupCavageHttpSigning httpSigning = new ChebancaGroupCavageHttpSigning(signer, signatureDTO.getSigningKid(), signatureDTO.getAlgorithm());
        headersToSign.put(HEADER_TPP_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        headersToSign.put(HEADER_DATE, FORMATTER.format(Instant.now(clock)));

        HttpHeaders headers = new HttpHeaders();
        headersToSign.forEach(headers::add);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(clientAccessToken);
        headers.add(HEADER_SIGNATURE, httpSigning.signHeaders(headersToSign, signatureDTO.getKeyId(), signatureDTO.getMethod(), signatureDTO.getPath()));
        return headers;
    }
}
