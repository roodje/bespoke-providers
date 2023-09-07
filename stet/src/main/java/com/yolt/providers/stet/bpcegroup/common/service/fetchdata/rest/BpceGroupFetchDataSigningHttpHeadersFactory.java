package com.yolt.providers.stet.bpcegroup.common.service.fetchdata.rest;

import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.springframework.http.HttpHeaders.DATE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BpceGroupFetchDataSigningHttpHeadersFactory extends FetchDataSigningHttpHeadersFactory {

    private Clock clock;

    public BpceGroupFetchDataSigningHttpHeadersFactory(HttpSigner httpSigner, Clock clock) {
        super(httpSigner);
        this.clock = clock;
    }

    @Override
    protected HttpHeaders prepareCommonHttpHeaders(String accessToken, String psuIpAddress, SignatureData signatureData) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(accessToken)
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .withCustomHeader(DATE, DateTimeFormatter.RFC_1123_DATE_TIME.withZone(clock.getZone()).format(Instant.now(clock)))
                .signAndBuild(signatureData, new byte[0]);
    }
}
