package com.yolt.providers.cbiglobe.common.pis.pec;

import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeDateUtil;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeSigningUtil;
import com.yolt.providers.cbiglobe.common.util.HttpUtils;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.util.Base64;

import static java.lang.Boolean.TRUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CbiGlobePisHttpHeadersFactory {

    static final String DIGEST_HEADER = "digest";
    static final String X_REQUEST_ID_HEADER = "x-request-id";
    static final String DATE_HEADER = "date";
    static final String TPP_REDIRECT_URI_HEADER = "tpp-redirect-uri";

    private static final String SIGNATURE_HEADER = "signature";
    private static final String ASPSP_CODE_HEADER = "aspsp-code";
    private static final String ASPSP_PRODUCT_CODE_HEADER = "aspsp-product-code";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER = "tpp-signature-certificate";
    private static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";

    private static final String BEARER = "Bearer ";

    public HttpHeaders createPaymentInitiationHttpHeaders(String accessToken,
                                                          AspspData aspspData,
                                                          SignatureData signatureData,
                                                          String psuIpAddress,
                                                          String redirectUrlWithState,
                                                          InitiatePaymentRequest paymentRequest,
                                                          Clock clock) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        headers.add(ASPSP_PRODUCT_CODE_HEADER, aspspData.getProductCode());
        headers.add(TPP_REDIRECT_URI_HEADER, redirectUrlWithState);
        headers.add(TPP_REDIRECT_PREFERRED_HEADER, TRUE.toString());
        return createSignedHeaders(headers, accessToken, aspspData.getCode(), paymentRequest, signatureData, clock);
    }

    public HttpHeaders createPaymentStatusHeaders(String accessToken,
                                                  AspspData aspspData,
                                                  SignatureData signatureData,
                                                  Clock clock) {
        HttpHeaders headers = new HttpHeaders();
        return createSignedHeaders(headers, accessToken, aspspData.getCode(), new byte[]{}, signatureData, clock);
    }

    public HttpHeaders createClientCredentialsHeaders(String clientId,
                                                      String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, HttpUtils.basicCredentials(clientId, clientSecret));
        headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }

    private static HttpHeaders createSignedHeaders(HttpHeaders headers,
                                                   String accessToken,
                                                   String aspspCode,
                                                   Object requestBody,
                                                   SignatureData signatureData,
                                                   Clock clock) {
        headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, BEARER + accessToken);
        headers.set(ASPSP_CODE_HEADER, aspspCode);
        headers.set(DIGEST_HEADER, CbiGlobeSigningUtil.getDigest(requestBody));
        headers.add(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.set(DATE_HEADER, CbiGlobeDateUtil.formattedCurrentDateTime(clock));
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER, Base64.getEncoder().encodeToString(signatureData.getEncodedSigningCertificate()));
        headers.set(SIGNATURE_HEADER, CbiGlobeSigningUtil.getSignature(headers, signatureData));
        return headers;
    }
}
