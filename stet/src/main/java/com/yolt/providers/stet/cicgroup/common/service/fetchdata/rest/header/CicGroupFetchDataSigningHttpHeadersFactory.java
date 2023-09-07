package com.yolt.providers.stet.cicgroup.common.service.fetchdata.rest.header;

import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import org.springframework.http.HttpHeaders;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.springframework.http.HttpHeaders.DATE;
import static org.springframework.http.HttpHeaders.HOST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class CicGroupFetchDataSigningHttpHeadersFactory extends FetchDataSigningHttpHeadersFactory {

    public CicGroupFetchDataSigningHttpHeadersFactory(HttpSigner httpSigner) {
        super(httpSigner);
    }

    @Override
    protected HttpHeaders prepareCommonHttpHeaders(String accessToken, String psuIpAddress, SignatureData signatureData) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(accessToken)
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .withCustomHeader(DATE, createRfc7321Date())
                .withCustomHeader(HOST, signatureData.getHost())
                .signAndBuild(signatureData, new byte[0]);
    }

    private String createRfc7321Date() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date());
    }
}
