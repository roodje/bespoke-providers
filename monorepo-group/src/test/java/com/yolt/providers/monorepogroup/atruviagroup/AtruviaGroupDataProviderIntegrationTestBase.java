package com.yolt.providers.monorepogroup.atruviagroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaAccessMeans;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.StepState;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.ZoneId;
import java.util.UUID;

abstract class AtruviaGroupDataProviderIntegrationTestBase {
    public static final String SELECTED_REGIONAL_BANK_ID = "82064188";

    protected static final String PSU_IP_ADDRESS = "127.0.0.1";
    protected static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    protected static final String STATE = "7c3e98de-0239-4868-ada8-aefb5384ef0a";
    protected static final String CONSENT_ID = "1234-wertiq-983";
    protected static final String AUTHORISATION_ID = "123auth456";
    protected static final String KEY_ID_VALUE = "11111111-1111-1111-1111-111111111111";
    protected static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
    protected static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtixrPVyrHFwff8Tv6dppJ6Pw1CJdMhWzMr7QqJg76/adNUQMl5f6C7b6/9l9nOeqdBqxyOycf3mianK0gHrVN3rwyPmh98yJiA/BeMzunMOe115vZew2d8XpEDXx8mbLSMnm7bKprKgvRKYf6B93ZfPTLosP6kabv12Pz9LR5NCx6joT55ii6uSFrvCilsvZRoeNxxOPPBCvAgd9sOxwROBUsXrgsgmpUzSuLxtdoRfNX7bPBi+znofAxChenDOcSGCv5lZ+0TKVhNzg0nsijjqOe5skwhRFBNGHj6Dg70tS5IcsQtz0hNRQCxBzDRrbn5TTBNo0So5G22g4l6cYJwIDAQAB";
    protected static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2LGs9XKscXB9/xO/p2mkno/DUIl0yFbMyvtComDvr9p01RAyXl/oLtvr/2X2c56p0GrHI7Jx/eaJqcrSAetU3evDI+aH3zImID8F4zO6cw57XXm9l7DZ3xekQNfHyZstIyebtsqmsqC9Eph/oH3dl89Muiw/qRpu/XY/P0tHk0LHqOhPnmKLq5IWu8KKWy9lGh43HE488EK8CB32w7HBE4FSxeuCyCalTNK4vG12hF81fts8GL7Oeh8DEKF6cM5xIYK/mVn7RMpWE3ODSeyKOOo57myTCFEUE0YePoODvS1LkhyxC3PSE1FALEHMNGtuflNME2jRKjkbbaDiXpxgnAgMBAAECggEAM18pSp+EPTYZR92QTiDiQGRxuFCMeWA3LVsz2ic71pmv0WKELoeT4pPSCi4ZxxJpNd6FPgTDSQtS6rO4BPceg0uu9O24Z/mM2wqeY2Ne8mQTueYOge5vmaz8wS6FMPcd4kPVVDhqsp6m3bP/EWU2NVDhv6FFdqo8p2VJ9bAcsmpQ4E+2ZJxPgSHqtrhn8W96FRRBecNcM7rpgfmEqa3pq9IdAdtw+XvWsqoig6NLHDg65tW6hkV2WbZkBr56R2fledlXczlXkVPZ3Od+U0gwQqwNkUYKvZvfgPcKANvHdYfBsEIm+2VRt31QE8ataYGQbdIHm0l5e0zJzMTfg3zmSQKBgQDzxbIsnry9fasgclgAa8pGwvL0y47QuqnGwguaMtzKHYbi7jSz5rBx5PJxKD8wthjPXKRyqC1UOwz7fjZ99MT1JdIDbQ/LSkKldb4ouUSU1lU3RVR9kBcQrgMGyR5i3Xi2zw9e6P4r9o1klPECK0vJ3fd03txrAHDuhrqLH6YelQKBgQC/T7owx3iPu01te9RI/Zfft7qSf90JptQkLO/evQnaoQdmp2I6TRbrBAeSierHbj1aQILAYQ6RUqckcupekicth/+ei2lSCnTxTyOpQI9ISzHCWmvjI1M5ZbfCJQ2XKk9mYRMiyHJ1bqN1IsdBj5VVuRprFPPCtoDX0US3c3t4ywKBgQCaRVvL9y1U8mnRH+vnYE/j1k4xc31PRUJagcUb8eJemq8ZgEykKXMysQRpbmIHLsamvGdqFFqTesdthWNw9O2Mg0HUXznmmnlxAwGz/gOT+cx2LQ8aY4zlRmqt6ausP6K8dm+wzdzE78RtigC4MbRF7Y5ETSHLKb1Ohr8Zeo8DvQKBgAvvtIVAnNQS8qTHGhqnv+cUdo6Xbbohb5EGQL0b/FZov6Z3ARj0IF7vdG1/L2fcB/XumnnYVGlax9TtWpQl+E3N83P37M1Sm7NGpcn0njv7fRJMQ/j7BkFJiGqTl0J8QFH58pC0Avgyu/4d+mKry7x6fRx7RS475tQQWYI8sVJ7AoGASAbtR9PXyJxkPOFYA0OkO6TCjkKtrwNt2J/ozZqTvydnV4RZMFNMy6XlHEqJvghvzbPIAz7g0UCA8Gln4eC/zpFAJdarPz54AdYSdsRk6m7jroASQ5+BxxtV6nSi4ljgYhNy81QNB35Jt5sgOrRIsib4Qv74LC6NcoFHAMIauMA=";
    protected static final String USERNAME = "John";

    @Autowired
    @Qualifier("VolksbankenRaiffeisen")
    protected ObjectMapper objectMapper;

    @SneakyThrows
    protected  <T> T extractExpectedProviderState(Class<T> clazz, String jsonString) {
        return objectMapper.readValue(jsonString, clazz);
    }

    @SneakyThrows
    protected String toAccessMeansString(StepState stepState) {
        return objectMapper.writeValueAsString(stepState);
    }

    @SneakyThrows
    protected String toAccessMeansString(AtruviaAccessMeans accessMeans) {
        return objectMapper.writeValueAsString(accessMeans);
    }

}
