package com.yolt.providers.openbanking.ais.generic2.pec.auth;

import org.springframework.web.util.UriComponentsBuilder;

public class GenericPaymentRedirectUrlExtractor {

    public String extractPureRedirectUrl(String redirectUrlPostedBackFromSite) {
        return UriComponentsBuilder.fromUriString(redirectUrlPostedBackFromSite)
                .replaceQuery(null)
                .fragment(null)
                .build()
                .toString();
    }
}
