package com.yolt.providers.openbanking.ais.common;

import org.springframework.http.HttpHeaders;

public class HttpUtils {

    private HttpUtils() {
    }

    public static String basicCredentials(String userName, String password) {
        String encoded = HttpHeaders.encodeBasicAuth(userName, password, null);
        return "Basic " + encoded;
    }
}
