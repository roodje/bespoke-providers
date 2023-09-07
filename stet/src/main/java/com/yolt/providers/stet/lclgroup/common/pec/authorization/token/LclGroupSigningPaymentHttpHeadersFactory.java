package com.yolt.providers.stet.lclgroup.common.pec.authorization.token;

import com.nimbusds.jose.jwk.JWK;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.common.StetSigningPaymentHttpHeadersFactory;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.function.Supplier;

import static org.springframework.http.HttpHeaders.DATE;

public class LclGroupSigningPaymentHttpHeadersFactory extends StetSigningPaymentHttpHeadersFactory {

    private final Supplier<String> lastExternalTraceIdSupplier;
    private final Clock clock;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss z")
            .withZone(ZoneId.of("Europe/Paris")).withLocale(Locale.ENGLISH);

    public LclGroupSigningPaymentHttpHeadersFactory(HttpSigner httpSigner, Clock clock) {
        super(httpSigner);
        this.lastExternalTraceIdSupplier = ExternalTracingUtil::createLastExternalTraceId;
        this.clock = clock;
    }

    @SneakyThrows
    @Override
    protected SignatureData prepareSignatureData(Signer signer, DefaultAuthenticationMeans authMeans, HttpMethod httpMethod, String url) {
        return new SignatureData(
                signer,
                JWK.parse(authMeans.getClientSigningCertificate()).getKeyID(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                httpMethod,
                url);
    }

    @Override
    public HttpHeaders createPaymentAccessTokenHttpHeaders(StetTokenPaymentPreExecutionResult preExecutionResult,
                                                           MultiValueMap<String, String> requestBody) {
        return HttpHeadersBuilder.builder()
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .withCustomXRequestId(ExternalTracingUtil.createLastExternalTraceId())
                .build();
    }

    @Override
    protected HttpHeaders prepareCommonHttpHeaders(SignatureData signatureData,
                                                   String accessToken,
                                                   String psuIpAddress,
                                                   Object requestBody) {
        String formattedDate = DATE_TIME_FORMATTER.format(Instant.now(clock));
        return HttpHeadersBuilder.builder(httpSigner)
                .withBearerAuthorization(accessToken)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier.get().replace("-", ""))
                .withCustomHeader(DATE, formattedDate)
                .signAndBuild(signatureData, requestBody);
    }
}
