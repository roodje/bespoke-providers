package com.yolt.providers.stet.lclgroup.common.auth;

import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import com.yolt.providers.stet.lclgroup.lcl.configuration.LclStetProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.DIGEST;
import static org.springframework.http.HttpHeaders.DATE;

public class LclFetchDataSigningHttpHeadersFactory extends FetchDataSigningHttpHeadersFactory {

    private final LclStetProperties properties;
    private final String providerIdentifier;
    private final Clock clock;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss z")
            .withZone(ZoneId.of("Europe/Paris")).withLocale(Locale.ENGLISH);

    public LclFetchDataSigningHttpHeadersFactory(
            final HttpSigner httpSigner,
            final LclStetProperties properties,
            final String providerIdentifier,
            final Clock clock) {
        super(httpSigner);
        this.properties = properties;
        this.providerIdentifier = providerIdentifier;
        this.clock = clock;
    }

    @Override
    protected SignatureData prepareSignatureData(final DataRequest dataRequest, final String endpoint, final HttpMethod method) {
        LclGroupClientConfiguration clientConfiguration = (LclGroupClientConfiguration) dataRequest.getAuthMeans();
        return new SignatureData(
                dataRequest.getSigner(),
                clientConfiguration.getCertificateUrl(properties.getS3baseUrl(), providerIdentifier),
                clientConfiguration.getClientSigningKeyId(),
                clientConfiguration.getClientSigningCertificate(),
                method,
                URI.create(dataRequest.getBaseUrl()).getHost(),
                endpoint);
    }

    @Override
    protected HttpHeaders prepareCommonHttpHeaders(String accessToken,
                                                   String psuIpAddress,
                                                   SignatureData signatureData) {
        String formattedDate = DATE_TIME_FORMATTER.format(Instant.now(clock));
        HttpHeaders headers = HttpHeadersBuilder.builder(httpSigner)
                .withBearerAuthorization(accessToken)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .withCustomHeader(DATE, formattedDate)
                .signAndBuild(signatureData, new byte[0]);
        headers.remove(DIGEST);
        return headers;
    }
}