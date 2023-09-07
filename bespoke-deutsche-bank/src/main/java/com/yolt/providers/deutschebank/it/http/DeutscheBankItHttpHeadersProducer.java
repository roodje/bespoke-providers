package com.yolt.providers.deutschebank.it.http;

import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpHeadersProducer;
import org.springframework.http.HttpHeaders;

public class DeutscheBankItHttpHeadersProducer extends DeutscheBankGroupHttpHeadersProducer {

    private static final String ASPSP_SCA_APPROACH_HEADER = "aspsp-sca-approach";
    private static final String CURRENCY_HEADER = "currency";

    public DeutscheBankItHttpHeadersProducer(DeutscheBankGroupProperties properties) {
        super(properties);
    }

    @Override
    public HttpHeaders createConsentHeaders(String psuIpAddress, String psuId, String redirectUri, String nokRedirectUri) {
        var consentHeaders = super.createConsentHeaders(psuIpAddress, psuId, redirectUri, nokRedirectUri);
        consentHeaders.add(ASPSP_SCA_APPROACH_HEADER, "REDIRECT");
        consentHeaders.add(CURRENCY_HEADER, "EUR");
        return consentHeaders;
    }
}
