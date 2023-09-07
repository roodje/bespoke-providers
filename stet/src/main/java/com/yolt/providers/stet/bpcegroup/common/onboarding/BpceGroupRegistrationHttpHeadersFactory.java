package com.yolt.providers.stet.bpcegroup.common.onboarding;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.http.signer.signature.Fingerprint;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class BpceGroupRegistrationHttpHeadersFactory implements RegistrationHttpHeadersFactory {

    private static final String PEM = ".pem";

    private final HttpSigner httpSigner;
    private final DefaultProperties properties;

    @SneakyThrows
    @Override
    public HttpHeaders createRegistrationHttpHeaders(RegistrationRequest registrationRequest, Object body, HttpMethod method, String url) {
        if (!(registrationRequest instanceof BpceGroupRegistrationRequest)) {
            throw new IllegalStateException("BpceGroupRegistrationHttpHeadersFactory requires Registration Request with Authorization Header");
        }
        var registrationRequestWithAuthorizationToken = (BpceGroupRegistrationRequest) registrationRequest;
        var accessToken = registrationRequestWithAuthorizationToken.getRegistrationAccessToken();
        var clientSigningCertificate = registrationRequest.getAuthMeans().getClientSigningCertificate();
        var signingKeyIdHeader = getCertificateUrl(clientSigningCertificate, properties.getS3baseUrl());
        var clientSigningKeyId = registrationRequest.getAuthMeans().getClientSigningKeyId();
        var registrationUrl = URI.create(url);
        var signatureData = new SignatureData(
                registrationRequest.getSigner(),
                signingKeyIdHeader,
                clientSigningKeyId,
                clientSigningCertificate,
                method,
                registrationUrl.getHost(),
                registrationUrl.getPath());
        var commonHeaders = prepareCommonHttpHeaders(registrationRequest.getLastExternalTraceIdSupplier(), accessToken);
        return HttpHeadersBuilder.enhancing(commonHeaders, httpSigner)
                .signAndBuild(signatureData, body);
    }

    private HttpHeaders prepareCommonHttpHeaders(Supplier<String> lastExternalTraceIdSupplier, String registrationAccessToken) {
        return HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_JSON)
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .withBearerAuthorization(registrationAccessToken)
                .build();
    }

    private String getCertificateUrl(X509Certificate certificate, String s3BaseUrl) throws CertificateEncodingException {
        var fingerprint = new Fingerprint(certificate.getEncoded()).toString();
        return s3BaseUrl + "/" + fingerprint + PEM;
    }
}
