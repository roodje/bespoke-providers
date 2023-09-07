package com.yolt.providers.openbanking.ais.generic2;

import lombok.Data;

import java.util.List;

@Data
public abstract class DefaultProperties {

    private List<CertificatePin> certificatePinning;

    private KeyConfiguration transportTruststoreConfig;

    private String baseUrl;

    private String oAuthTokenUrl;
    private String oAuthAuthorizationUrl;

    private String clientId;

    private int paginationLimit;

    private String audience;

    @Data
    public static class CertificatePin {

        private String hostname;
        private List<String> chain;
    }

    @Data
    public static class KeyConfiguration {

        private String file;
        private String password;
        private String alias;
        private String id;
    }
}
