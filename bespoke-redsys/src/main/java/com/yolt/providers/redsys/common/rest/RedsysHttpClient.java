package com.yolt.providers.redsys.common.rest;

import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.dto.*;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.SignatureData;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.common.util.RedsysSigningUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
public class RedsysHttpClient {

    protected static final String DIGEST_HEADER = "digest";
    protected static final String REQUEST_ID_HEADER = "X-Request-ID";
    protected static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
    protected static final String TPP_REDIRECT_PREFERRED= "TPP-Redirect-Preferred";

    protected static final List<String> SIGNING_HEADERS_FOR_CONSENT = Arrays.asList(DIGEST_HEADER, REQUEST_ID_HEADER, TPP_REDIRECT_URI_HEADER, TPP_REDIRECT_PREFERRED);
    protected static final List<String> SIGNING_HEADERS_FOR_FETCH = Arrays.asList(DIGEST_HEADER, REQUEST_ID_HEADER);
    protected static final String CODE_PARAM = "code";
    protected static final String CLIENT_ID_PARAM = "client_id";
    protected static final String CODE_VERIFIER_PARAM = "code_verifier";
    protected static final String REDIRECT_URI_PARAM = "redirect_uri";
    protected static final String REFRESH_TOKEN = "refresh_token";
    protected static final String CONSENT_ID_HEADER = "Consent-ID";

    protected static final String CONSENT_URL = "/v1/consents";
    protected static final String ACCOUNT_URL = "/v1/accounts";
    protected static final String ACCOUNT_WITH_BALANCES_URL = "/v1/accounts?withBalance=true";
    protected static final String TOKEN_URL_TEMPLATE = "/token?grant_type=authorization_code&client_id={client_id}&code_verifier={code_verifier}&code={code}&redirect_uri={redirect_uri}";
    protected static final String REFRESH_TOKEN_URL_TEMPLATE = "/token?grant_type=refresh_token&client_id={client_id}&refresh_token={refresh_token}";
    protected static final String TRANSACTION_URL_TEMPLATE = "/v1/accounts/{accountid}/transactions?bookingStatus={bookingStatus}&dateFrom={dateFrom}";
    protected static final String BALANCE_URL_TEMPLATE = "/v1/accounts/{accountid}/balances";
    protected static final String SIGNATURE_HEADER = "signature";
    protected static final String TPP_SIGNATURE_CERTIFICATE_HEADER = "TPP-Signature-Certificate";
    protected static final String PSU_IP_ADDRESS_HEADER = "PSU-IP-Address";
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected final RestOperations restOperations;

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

    public Token getAccessToken(final RedsysAuthenticationMeans authenticationMeans,
                                final String redirectUrl,
                                final String authorizationCode,
                                final String codeVerifier,
                                final String authorizationUrl) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(CODE_PARAM, authorizationCode);
        queryParams.put(REDIRECT_URI_PARAM, redirectUrl);
        queryParams.put(CLIENT_ID_PARAM, authenticationMeans.getClientId());
        queryParams.put(CODE_VERIFIER_PARAM, codeVerifier);

        return restOperations.postForEntity(
                authorizationUrl + TOKEN_URL_TEMPLATE,
                new HttpEntity<>(createHeaderForToken()),
                Token.class,
                queryParams).getBody();
    }

    public Token getNewAccessTokenUsingRefreshToken(final RedsysAuthenticationMeans authenticationMeans,
                                                    final String refreshToken,
                                                    final String authorizationUrl,
                                                    final String psuIdAddress) throws TokenInvalidException { //NOSONAR TokenInvalidException is checked. Inherited class is throwing TokenInvalidException.
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(REFRESH_TOKEN, refreshToken);
        queryParams.put(CLIENT_ID_PARAM, authenticationMeans.getClientId());
        return restOperations.postForEntity(
                authorizationUrl + REFRESH_TOKEN_URL_TEMPLATE,
                new HttpEntity<>(createHeaderForToken()),
                Token.class,
                queryParams).getBody();
    }

    public ResponseAccountsList getAllUserAccounts(final String userAccessToken,
                                                   final String consentId,
                                                   final SignatureData signatureData,
                                                   final String psuIpAddress) {
        HttpHeaders headers = addTokenAndConsent(addSignatureRelatedHeaders(createHeaderWithNewRequestId(), signatureData, psuIpAddress), userAccessToken, consentId);

        return restOperations.exchange(
                ACCOUNT_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseAccountsList.class).getBody();
    }

    public ResponseAccountsList getAllUserAccountsAndBalances(final String userAccessToken,
                                                              final String consentId,
                                                              final SignatureData signatureData,
                                                              final String psuIpAddress) {
        HttpHeaders headers = addTokenAndConsent(addSignatureRelatedHeaders(createHeaderWithNewRequestId(), signatureData, psuIpAddress), userAccessToken, consentId);

        return restOperations.exchange(
                ACCOUNT_WITH_BALANCES_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseAccountsList.class).getBody();
    }

    public ResponseAccountBalances getBalanceForAccount(final String userAccessToken,
                                                        final String consentId,
                                                        final String resourceId,
                                                        final SignatureData signatureData,
                                                        final String psuIpAddress) {
        HttpHeaders headers = addTokenAndConsent(addSignatureRelatedHeaders(createHeaderWithNewRequestId(), signatureData, psuIpAddress), userAccessToken, consentId);

        return restOperations.exchange(
                BALANCE_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseAccountBalances.class,
                resourceId).getBody();
    }

    public ResponseAccountTransactions getTransactionForGivenAccount(final RedsysAccessMeans accessMeans,
                                                                     final SignatureData signatureData,
                                                                     final String psuIpAddress,
                                                                     final String givenRequestId,
                                                                     final String resourceId,
                                                                     final Instant transactionsFetchStartTime,
                                                                     final BookingStatus bookingStatus) {
        String userAccessToken = accessMeans.getToken().getAccessToken();
        String consentId = accessMeans.getConsentId();

        if (StringUtils.isEmpty(consentId)) {
            throw new MissingDataException("Missing consent id");
        }
        if (StringUtils.isEmpty(resourceId)) {
            throw new MissingDataException("Missing resource id");
        }

        String formattedFromBookingDateTime = DATE_TIME_FORMATTER
                .format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountid", resourceId);
        queryParams.put("dateFrom", formattedFromBookingDateTime);
        queryParams.put("bookingStatus", bookingStatus.getStatus());

        HttpHeaders headers = addTokenAndConsent(addSignatureRelatedHeaders(createHeaderWithGivenRequestId(givenRequestId), signatureData, psuIpAddress), userAccessToken, consentId);

        return restOperations.exchange(
                TRANSACTION_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseAccountTransactions.class,
                queryParams).getBody();
    }

    public ResponseAccountTransactions getNextPageOfTransactions(final RedsysAccessMeans accessMeans,
                                                                 final SignatureData signatureData,
                                                                 final String psuIpAddress,
                                                                 final String givenRequestId,
                                                                 final String path) {
        String userAccessToken = accessMeans.getToken().getAccessToken();
        String consentId = accessMeans.getConsentId();

        if (StringUtils.isEmpty(consentId)) {
            throw new MissingDataException("Missing consent id");
        }

        HttpHeaders headers = addTokenAndConsent(addSignatureRelatedHeaders(createHeaderWithGivenRequestId(givenRequestId), signatureData, psuIpAddress), userAccessToken, consentId);

        return restOperations.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseAccountTransactions.class).getBody();
    }

    private static HttpHeaders createHeaderWithGivenRequestId(final String traceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(REQUEST_ID_HEADER, traceId);
        return headers;
    }

    private static HttpHeaders createHeaderWithNewRequestId() {
        return createHeaderWithGivenRequestId(ExternalTracingUtil.createLastExternalTraceId());
    }

    protected static HttpHeaders createHeaderForToken() {
        HttpHeaders headers = createHeaderWithNewRequestId();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }

    private static HttpHeaders createSignatureRelatedHeadersForConsent(final Object consentObject,
                                                                       final SignatureData signatureData,
                                                                       final String psuIpAddress,
                                                                       final String baseClientRedirectUrl) {
        HttpHeaders headers = createHeaderWithNewRequestId();
        headers.add(TPP_REDIRECT_URI_HEADER, baseClientRedirectUrl);
        headers.add(TPP_REDIRECT_PREFERRED, "true");
        headers.add(DIGEST_HEADER, RedsysSigningUtil.calculateDigest(consentObject));
        headers.add(SIGNATURE_HEADER, RedsysSigningUtil.calculateSignature(headers, signatureData, SIGNING_HEADERS_FOR_CONSENT)); //TODO there could be discrepancy between headers and SIGNING_HEADERS...
        headers.add(TPP_SIGNATURE_CERTIFICATE_HEADER, Base64.getEncoder().encodeToString(signatureData.getEncodedSigningCertificate()));
        if (psuIpAddress != null) {
            headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        return headers;
    }

    private static HttpHeaders addSignatureRelatedHeaders(final HttpHeaders headers,
                                                          final SignatureData signatureData,
                                                          final String psuIpAddress) {
        headers.add(DIGEST_HEADER, RedsysSigningUtil.calculateDigest(new byte[0])); // TODO precompute Digest(new byte[0])
        headers.add(SIGNATURE_HEADER, RedsysSigningUtil.calculateSignature(headers, signatureData, SIGNING_HEADERS_FOR_FETCH)); //TODO there could be discrepancy between headers and SIGNING_HEADERS...
        headers.add(TPP_SIGNATURE_CERTIFICATE_HEADER, Base64.getEncoder().encodeToString(signatureData.getEncodedSigningCertificate()));
        if (psuIpAddress != null) {
            headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        return headers;
    }

    private static HttpHeaders addTokenAndConsent(final HttpHeaders headers,
                                                  final String userAccessToken,
                                                  final String consentId) {

        headers.add(CONSENT_ID_HEADER, consentId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userAccessToken);
        return headers;
    }
}
