package com.yolt.providers.rabobank.pis.pec;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


@RequiredArgsConstructor
public class RabobankCommonHttpHeaderProvider {

    private static final DateTimeFormatter RABOBANK_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O", Locale.ENGLISH);
    private static final String PSU_IP_ADDRESS_NAME = "PSU-IP-Address";
    private static final String X_REQUEST_ID_NAME = "x-request-id";
    private static final String X_IBM_CLIENT_ID_NAME = "x-ibm-client-id";
    private static final String DATE_NAME = "date";

    private final Clock clock;

    public HttpHeaders providerCommonHttpHeaders(String psuIpAddress, String clientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.add(PSU_IP_ADDRESS_NAME, psuIpAddress);
        }
        headers.add(X_REQUEST_ID_NAME, ExternalTracingUtil.createLastExternalTraceId());
        headers.add(X_IBM_CLIENT_ID_NAME, clientId);
        headers.add(DATE_NAME, RABOBANK_DATETIME_FORMATTER.format(ZonedDateTime.now(clock)));
        return headers;
    }
}
