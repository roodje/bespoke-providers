package com.yolt.providers.abnamrogroup.common.pis.pec;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class AbnAmroAuthorizationHttpHeadersProvider {

    public HttpHeaders provideHttpHeadersForPisToken() {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return httpHeaders;
    }
}
