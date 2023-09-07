package com.yolt.providers.stet.societegeneralegroup.common.service.fetchdata.rest.header;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.springframework.http.HttpHeaders.DATE;
import static org.springframework.http.HttpHeaders.HOST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class SocieteGeneraleGroupFetchDataSigningHttpHeadersFactory extends FetchDataSigningHttpHeadersFactory {

    protected static final String CLIENT_ID = "client-id";

    public SocieteGeneraleGroupFetchDataSigningHttpHeadersFactory(HttpSigner httpSigner) {
        super(httpSigner);
    }

    @Override
    protected SignatureData prepareSignatureData(DataRequest dataRequest,
                                                 String endpoint,
                                                 HttpMethod method) {
        DefaultAuthenticationMeans authMeans = dataRequest.getAuthMeans();
        return new SignatureData(
                dataRequest.getSigner(),
                authMeans.getSigningKeyIdHeader(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                method,
                URI.create(dataRequest.getBaseUrl()).getHost(),
                endpoint,
                authMeans.getClientId());
    }

    @Override
    protected HttpHeaders prepareCommonHttpHeaders(String accessToken, String psuIpAddress, SignatureData signatureData) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(accessToken)
                .withContentType(APPLICATION_JSON)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .withCustomHeader(CLIENT_ID, signatureData.getClientId())
                .signAndBuild(signatureData, new byte[0]);
    }
}
