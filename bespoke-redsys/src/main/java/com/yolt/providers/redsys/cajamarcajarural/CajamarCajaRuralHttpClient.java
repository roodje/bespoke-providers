package com.yolt.providers.redsys.cajamarcajarural;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.redsys.common.dto.RequestGetConsent;
import com.yolt.providers.redsys.common.dto.ResponseGetConsent;
import com.yolt.providers.redsys.common.model.SignatureData;
import com.yolt.providers.redsys.common.rest.RedsysHttpClient;
import com.yolt.providers.redsys.common.util.RedsysSigningUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestOperations;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class CajamarCajaRuralHttpClient extends RedsysHttpClient {

    protected static final List<String> SIGNING_HEADERS_FOR_CONSENT = Arrays.asList(DIGEST_HEADER, REQUEST_ID_HEADER, TPP_REDIRECT_URI_HEADER);

    public CajamarCajaRuralHttpClient(RestOperations restOperations) {
        super(restOperations);
    }

    @Override
    public ResponseGetConsent generateConsent(final RequestGetConsent consentObject,
                                              final String userAccessToken,
                                              final SignatureData signatureData,
                                              final String psuIpAddress,
                                              final String baseClientRedirectUrl) {
        HttpHeaders headers = createSignatureRelatedHeadersForConsent(consentObject, signatureData, psuIpAddress, baseClientRedirectUrl);

        headers.setBearerAuth(userAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return restOperations.postForEntity(
                CONSENT_URL,
                new HttpEntity<>(consentObject, headers),
                ResponseGetConsent.class).getBody();
    }

    private static HttpHeaders createSignatureRelatedHeadersForConsent(final Object consentObject,
                                                                       final SignatureData signatureData,
                                                                       final String psuIpAddress,
                                                                       final String baseClientRedirectUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.add(TPP_REDIRECT_URI_HEADER, baseClientRedirectUrl);
        headers.add(DIGEST_HEADER, RedsysSigningUtil.calculateDigest(consentObject));
        headers.add(SIGNATURE_HEADER, RedsysSigningUtil.calculateSignature(headers, signatureData, SIGNING_HEADERS_FOR_CONSENT));
        headers.add(TPP_SIGNATURE_CERTIFICATE_HEADER, Base64.getEncoder().encodeToString(signatureData.getEncodedSigningCertificate()));
        if (psuIpAddress != null) {
            headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        return headers;
    }
}
