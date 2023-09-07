package com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.rest.header;

import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Collections;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.springframework.http.HttpHeaders.DATE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class CreditAgricoleGroupFetchDataHttpHeadersFactory extends FetchDataSigningHttpHeadersFactory {

    private final Clock clock;

    public CreditAgricoleGroupFetchDataHttpHeadersFactory(HttpSigner httpSigner, Clock clock) {
        super(httpSigner);
        this.clock = clock;
    }

    @Override
    protected HttpHeaders prepareCommonHttpHeaders(String accessToken, String psuIpAddress, SignatureData signatureData) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withContentType(APPLICATION_JSON)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .withCustomHeader(DATE, RFC_1123_DATE_TIME.format(clock.instant().atZone(ZoneOffset.UTC.normalized())))
                .withBearerAuthorization(accessToken)
                .withPsuIpAddress(psuIpAddress)
                .signAndBuild(signatureData, new byte[0]);
    }
}
