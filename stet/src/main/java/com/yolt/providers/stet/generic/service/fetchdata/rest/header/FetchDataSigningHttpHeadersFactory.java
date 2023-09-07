package com.yolt.providers.stet.generic.service.fetchdata.rest.header;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Collections;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class FetchDataSigningHttpHeadersFactory implements FetchDataHttpHeadersFactory {

    protected final HttpSigner httpSigner;
    protected final Supplier<String> lastExternalTraceIdSupplier;

    public FetchDataSigningHttpHeadersFactory(HttpSigner httpSigner) {
        this.httpSigner = httpSigner;
        this.lastExternalTraceIdSupplier = ExternalTracingUtil::createLastExternalTraceId;
    }

    @Override
    public HttpHeaders createFetchDataHeaders(String endpoint, DataRequest dataRequest, HttpMethod method) {
        SignatureData signatureData = prepareSignatureData(
                dataRequest,
                endpoint,
                method);

        return prepareCommonHttpHeaders(dataRequest.getAccessToken(), dataRequest.getPsuIpAddress(), signatureData);
    }

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
                endpoint);
    }

    protected HttpHeaders prepareCommonHttpHeaders(String accessToken,
                                                   String psuIpAddress,
                                                   SignatureData signatureData) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(accessToken)
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .signAndBuild(signatureData, new byte[0]);
    }
}
