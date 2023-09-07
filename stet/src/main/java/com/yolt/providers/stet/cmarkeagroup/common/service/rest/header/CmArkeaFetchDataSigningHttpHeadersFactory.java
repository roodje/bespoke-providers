package com.yolt.providers.stet.cmarkeagroup.common.service.rest.header;

import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

public class CmArkeaFetchDataSigningHttpHeadersFactory extends FetchDataSigningHttpHeadersFactory {

    private final String version;

    public CmArkeaFetchDataSigningHttpHeadersFactory(HttpSigner httpSigner, String version) {
        super(httpSigner);
        this.version = version;
    }

    @Override
    protected HttpHeaders prepareCommonHttpHeaders(String accessToken,
                                                   String psuIpAddress,
                                                   SignatureData signatureData) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(MediaType.valueOf("application/hal+json;charset=utf-8")))
                .withCustomHeader("Accept", "application/hal+json;charset=utf-8;version=" + version)
                .withBearerAuthorization(accessToken)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .signAndBuild(signatureData, new byte[0]);
    }
}
