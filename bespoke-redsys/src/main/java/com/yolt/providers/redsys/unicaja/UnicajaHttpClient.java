package com.yolt.providers.redsys.unicaja;

import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.redsys.common.dto.ResponseAccountTransactions;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.SignatureData;
import com.yolt.providers.redsys.common.rest.BookingStatus;
import com.yolt.providers.redsys.common.rest.RedsysHttpClient;
import com.yolt.providers.redsys.common.util.RedsysSigningUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class UnicajaHttpClient extends RedsysHttpClient {

    public UnicajaHttpClient(final RestOperations restOperations) {
        super(restOperations);
    }

    @Override
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

        HttpHeaders headers = addTransactionsHeaders(addSignatureRelatedHeaders(createHeaderWithGivenRequestId(givenRequestId), signatureData, psuIpAddress), userAccessToken, consentId);

        return restOperations.exchange(
                TRANSACTION_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseAccountTransactions.class,
                queryParams).getBody();
    }

    @Override
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

        HttpHeaders headers = addTransactionsHeaders(addSignatureRelatedHeaders(createHeaderWithGivenRequestId(givenRequestId), signatureData, psuIpAddress), userAccessToken, consentId);

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

    private static HttpHeaders addTransactionsHeaders(final HttpHeaders headers,
                                                      final String userAccessToken,
                                                      final String consentId) {

        headers.add(CONSENT_ID_HEADER, consentId);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userAccessToken);
        return headers;
    }
}
