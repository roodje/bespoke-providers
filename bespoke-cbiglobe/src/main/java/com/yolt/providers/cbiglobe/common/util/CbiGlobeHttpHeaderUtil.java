package com.yolt.providers.cbiglobe.common.util;

import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.util.Base64;

import static java.lang.Boolean.TRUE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CbiGlobeHttpHeaderUtil {

    public static final String CPAAS_TOTAL_PAGES_HEADER = "cpaas-total-pages";

    static final String DIGEST_HEADER = "digest";
    static final String X_REQUEST_ID_HEADER = "x-request-id";
    static final String DATE_HEADER = "date";
    static final String TPP_REDIRECT_URI_HEADER = "tpp-redirect-uri";
    static final String PSU_ID = "psu-id";

    private static final String CONSENT_ID_HEADER = "consent-id";
    private static final String SIGNATURE_HEADER = "signature";
    private static final String CPAAS_TRANSACTION_ID_HEADER = "cpaas-transaction-id";
    private static final String OPERATION_NAME_HEADER = "operation-name";
    private static final String ASPSP_CODE_HEADER = "aspsp-code";
    private static final String ASPSP_PRODUCT_CODE_HEADER = "aspsp-product-code";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER = "tpp-signature-certificate";
    private static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    private static final String ACCEPT_HEADER = "accept";
    private static final String PSU_IP_ADDRESS = "psu-ip-address";
    private static final String ASPSP_PRODUCT_CODE = "aspsp-product-code";

    private static final String BEARER = "Bearer ";

    public static HttpHeaders getClientCredentialsHeaders(String clientId,
                                                          String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, HttpUtils.basicCredentials(clientId, clientSecret));
        headers.set(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }

    static HttpHeaders getAspspProductsHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, BEARER + accessToken);
        headers.add(CPAAS_TRANSACTION_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        return headers;
    }

    public static HttpHeaders getConsentCreationHeaders(String accessToken,
                                                        Object requestBody,
                                                        String callbackUrl,
                                                        SignatureData signingData,
                                                        AspspData aspspData,
                                                        Clock clock) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(TPP_REDIRECT_URI_HEADER, callbackUrl);
        headers.add(TPP_REDIRECT_PREFERRED_HEADER, TRUE.toString());
        headers.set(ASPSP_PRODUCT_CODE_HEADER, aspspData.getProductCode());
        return getSignedHeaders(headers, accessToken, aspspData.getCode(), requestBody, signingData, clock);
    }

    public static HttpHeaders getConsentUpdateHeaders(String accessToken,
                                                      String aspspCode,
                                                      Object requestBody,
                                                      String callbackUrl,
                                                      String operationType,
                                                      SignatureData signingData,
                                                      Clock clock) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(TPP_REDIRECT_URI_HEADER, callbackUrl);
        headers.add(OPERATION_NAME_HEADER, operationType);
        return getSignedHeaders(headers, accessToken, aspspCode, requestBody, signingData, clock);
    }

    public static HttpHeaders getConsentHeaders(String accessToken,
                                                SignatureData signingData,
                                                AspspData aspspData,
                                                Clock clock) {
        HttpHeaders headers = new HttpHeaders();
        return getSignedHeaders(headers, accessToken, aspspData.getCode(), new Byte[0], signingData, clock);
    }

    /**
     * These headers are generic for fetching accounts and balances data
     */
    public static HttpHeaders getFetchDataHeaders(String accessToken,
                                                  String aspspCode,
                                                  String consentId,
                                                  SignatureData signatureData,
                                                  String psuIpAddress,
                                                  Clock clock) {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.set(PSU_IP_ADDRESS, psuIpAddress);
        }
        headers.set(CONSENT_ID_HEADER, consentId);
        return getSignedHeaders(headers, accessToken, aspspCode, new Byte[0], signatureData, clock);
    }

    /**
     * These headers are required for fetching transactions data
     */
    public static HttpHeaders getTransactionsHeaders(String accessToken,
                                                     String aspspCode,
                                                     String consentId,
                                                     SignatureData signatureData,
                                                     String psuIpAddress,
                                                     Clock clock) {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.set(PSU_IP_ADDRESS, psuIpAddress);
        }
        headers.set(CONSENT_ID_HEADER, consentId);
        headers.set(ACCEPT_HEADER, "JSON");
        return getSignedHeaders(headers, accessToken, aspspCode, new Byte[0], signatureData, clock);
    }

    private static HttpHeaders getSignedHeaders(HttpHeaders headers,
                                                String accessToken,
                                                String aspspCode,
                                                Object requestBody,
                                                SignatureData signatureData,
                                                Clock clock) {
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        headers.set(AUTHORIZATION, BEARER + accessToken);
        headers.set(ASPSP_CODE_HEADER, aspspCode);
        headers.set(DIGEST_HEADER, CbiGlobeSigningUtil.getDigest(requestBody));
        headers.add(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.set(DATE_HEADER, CbiGlobeDateUtil.formattedCurrentDateTime(clock));
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER, Base64.getEncoder().encodeToString(signatureData.getEncodedSigningCertificate()));
        headers.set(SIGNATURE_HEADER, CbiGlobeSigningUtil.getSignature(headers, signatureData));
        return headers;
    }
}
