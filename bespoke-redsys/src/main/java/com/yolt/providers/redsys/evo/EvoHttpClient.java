package com.yolt.providers.redsys.evo;

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

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EvoHttpClient extends RedsysHttpClient {

    private final Clock clock;

    private static final String ACCOUNT_ID_PARAM = "accountid";
    private static final String DATE_FROM_PARAM = "dateFrom";
    private static final String DATE_TO_PARAM = "dateTo";
    private static final String BOOKING_STATUS_PARAM = "bookingStatus";

    private static final String TRANSACTION_URL_TEMPLATE = "/v1/accounts/{accountid}/transactions?bookingStatus={bookingStatus}&dateFrom={dateFrom}&dateTo={dateTo}";

    public EvoHttpClient(final RestOperations restOperations, final Clock clock) {
        super(restOperations);
        this.clock = clock;
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
                .format(OffsetDateTime.ofInstant(transactionsFetchStartTime, clock.getZone()));
        String formattedDateTo = DATE_TIME_FORMATTER
                .format(OffsetDateTime.ofInstant(Instant.now(clock), clock.getZone()));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(ACCOUNT_ID_PARAM, resourceId);
        queryParams.put(DATE_FROM_PARAM, formattedFromBookingDateTime);
        queryParams.put(DATE_TO_PARAM, formattedDateTo);
        queryParams.put(BOOKING_STATUS_PARAM, bookingStatus.getStatus());

        HttpHeaders headers = addTokenAndConsent(addSignatureRelatedHeaders(createHeaderWithGivenRequestId(givenRequestId), signatureData, psuIpAddress), userAccessToken, consentId);

        return restOperations.exchange(
                TRANSACTION_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ResponseAccountTransactions.class,
                queryParams).getBody();
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

    private static HttpHeaders addTokenAndConsent(final HttpHeaders headers,
                                                  final String userAccessToken,
                                                  final String consentId) {

        headers.add(CONSENT_ID_HEADER, consentId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userAccessToken);
        return headers;
    }
}
