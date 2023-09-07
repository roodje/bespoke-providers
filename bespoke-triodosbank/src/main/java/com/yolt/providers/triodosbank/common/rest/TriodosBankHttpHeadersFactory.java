package com.yolt.providers.triodosbank.common.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.triodosbank.common.model.domain.SignatureData;
import com.yolt.providers.triodosbank.common.model.http.ConsentCreationRequest;
import com.yolt.providers.triodosbank.common.util.TriodosBankSigningUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
class TriodosBankHttpHeadersFactory {

    private static final String DIGEST_HEADER = "digest";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER = "tpp-signature-certificate";
    private static final String SIGNATURE_HEADER = "signature";
    private static final String TPP_REDIRECT_URI_HEADER = "tpp-redirect-uri";
    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    private static final String CONSENT_ID_HEADER = "consent-id";

    private final ObjectMapper objectMapper;

    HttpHeaders createRegistrationTokenHeaders(SignatureData signatureData) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(DIGEST_HEADER, TriodosBankSigningUtil.getDigest(new byte[0]));
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.set(SIGNATURE_HEADER, TriodosBankSigningUtil.getSignature(headers, signatureData));
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER, signatureData.getBase64EncodedSigningCertificate());
        return headers;
    }

    HttpHeaders createRegistrationHeaders(SignatureData signatureData, String registrationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(DIGEST_HEADER, TriodosBankSigningUtil.getDigest(new byte[0]));
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.set(SIGNATURE_HEADER, TriodosBankSigningUtil.getSignature(headers, signatureData));
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(registrationToken);
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER, signatureData.getBase64EncodedSigningCertificate());
        return headers;
    }

    HttpHeaders createConsentCreationResponseHeaders(SignatureData signatureData,
                                                     String redirectUrl,
                                                     String psuIpAddress,
                                                     ConsentCreationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        byte[] serializedRequestBody = getSerializedRequestBody(request);

        headers.set(DIGEST_HEADER, TriodosBankSigningUtil.getDigest(serializedRequestBody));
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(APPLICATION_JSON);
        headers.set(TPP_REDIRECT_URI_HEADER, redirectUrl);
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER, signatureData.getBase64EncodedSigningCertificate());
        headers.set(SIGNATURE_HEADER, TriodosBankSigningUtil.getSignature(headers, signatureData));

        return headers;
    }

    HttpHeaders createConsentStatusHeaders(SignatureData signatureData) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(DIGEST_HEADER, TriodosBankSigningUtil.getDigest(new byte[0]));
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.set(SIGNATURE_HEADER, TriodosBankSigningUtil.getSignature(headers, signatureData));
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER, signatureData.getBase64EncodedSigningCertificate());
        return headers;
    }

    HttpHeaders createTokenHeaders(String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);
        return headers;
    }

    HttpHeaders createAuthorisationHeaders(SignatureData signatureData, String psuIpAddress, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(DIGEST_HEADER, TriodosBankSigningUtil.getDigest(new byte[0]));
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.set(SIGNATURE_HEADER, TriodosBankSigningUtil.getSignature(headers, signatureData));
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER, signatureData.getBase64EncodedSigningCertificate());
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        return headers;
    }

    HttpHeaders createFetchDataHeaders(SignatureData signatureData, String consentId, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(DIGEST_HEADER, TriodosBankSigningUtil.getDigest(new byte[0]));
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.set(SIGNATURE_HEADER, TriodosBankSigningUtil.getSignature(headers, signatureData));
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER, signatureData.getBase64EncodedSigningCertificate());
        headers.setBearerAuth(accessToken);
        headers.set(CONSENT_ID_HEADER, consentId);
        return headers;
    }

    private byte[] getSerializedRequestBody(final Object requestBody) {
        try {
            return objectMapper.writeValueAsString(requestBody).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Couldn't map request body.");
        }
    }

}
