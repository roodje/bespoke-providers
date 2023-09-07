package com.yolt.providers.stet.generic.service.registration.rest.header;

import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultRegistrationHttpHeadersFactory implements RegistrationHttpHeadersFactory {

    protected final HttpSigner httpSigner;

    @Override
    public HttpHeaders createRegistrationHttpHeaders(RegistrationRequest registrationRequest, Object body, HttpMethod method, String url) {
        String signingKeyIdHeader = registrationRequest.getAuthMeans().getSigningKeyIdHeader();
        UUID clientSigningKeyId = registrationRequest.getAuthMeans().getClientSigningKeyId();
        X509Certificate clientSigningCertificate = registrationRequest.getAuthMeans().getClientSigningCertificate();
        URI registrationUrl = URI.create(url);

        SignatureData signatureData = new SignatureData(
                registrationRequest.getSigner(),
                signingKeyIdHeader,
                clientSigningKeyId,
                clientSigningCertificate,
                method,
                registrationUrl.getHost(),
                registrationUrl.getPath());

        HttpHeaders commonHeaders = prepareCommonHttpHeaders(registrationRequest.getLastExternalTraceIdSupplier());
        return HttpHeadersBuilder.enhancing(commonHeaders, httpSigner)
                .signAndBuild(signatureData, body);
    }

    private HttpHeaders prepareCommonHttpHeaders(Supplier<String> lastExternalTraceIdSupplier) {
        return HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_JSON)
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .build();
    }
}
