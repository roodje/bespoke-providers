package com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal;

import com.yolt.providers.common.exception.FormDecryptionFailedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AtruviaFormDecryptorTest {

    private static final String JWE_WITH_TEST_PASSWORD_STRING = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIn0.IdrFauXttl9k7Y5q2_HdpKszHdlFJeYjFl3zNFELUNc7wqAIilsIf_33sCk0IbY5rwbog3waSGfDuJoKPowL-dLii8wK7wwtXetwKH8mRKrj6KhgficDUOb3I8NTsPSbbMUq7hH9uIR5I3K_J8bb-Y-9VCFAudQCFQ4s9HEa5rZpUpTDycvEiJKWgRBBVXddUsDHH0U2yIPASGyHdtyqWXOWinRaI7CMh7MUYSK4YjlGZ-ncMf06m61l2D8ftXhVwmNzV1bnhsOIBbXGzbzOsWLjEBuIF3DRmzjrh5v_O02Ckz5XcPXndnu9TASTmxBu-P2Lc3MWtuRWq-inSrAQ7Q.cMUj_Bdw8S5KYp00.3l8X8G9fOIsNKXjr.3g4uPO1l_Jh7CZ1PCg5l_Q";
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2LGs9XKscXB9/xO/p2mkno/DUIl0yFbMyvtComDvr9p01RAyXl/oLtvr/2X2c56p0GrHI7Jx/eaJqcrSAetU3evDI+aH3zImID8F4zO6cw57XXm9l7DZ3xekQNfHyZstIyebtsqmsqC9Eph/oH3dl89Muiw/qRpu/XY/P0tHk0LHqOhPnmKLq5IWu8KKWy9lGh43HE488EK8CB32w7HBE4FSxeuCyCalTNK4vG12hF81fts8GL7Oeh8DEKF6cM5xIYK/mVn7RMpWE3ODSeyKOOo57myTCFEUE0YePoODvS1LkhyxC3PSE1FALEHMNGtuflNME2jRKjkbbaDiXpxgnAgMBAAECggEAM18pSp+EPTYZR92QTiDiQGRxuFCMeWA3LVsz2ic71pmv0WKELoeT4pPSCi4ZxxJpNd6FPgTDSQtS6rO4BPceg0uu9O24Z/mM2wqeY2Ne8mQTueYOge5vmaz8wS6FMPcd4kPVVDhqsp6m3bP/EWU2NVDhv6FFdqo8p2VJ9bAcsmpQ4E+2ZJxPgSHqtrhn8W96FRRBecNcM7rpgfmEqa3pq9IdAdtw+XvWsqoig6NLHDg65tW6hkV2WbZkBr56R2fledlXczlXkVPZ3Od+U0gwQqwNkUYKvZvfgPcKANvHdYfBsEIm+2VRt31QE8ataYGQbdIHm0l5e0zJzMTfg3zmSQKBgQDzxbIsnry9fasgclgAa8pGwvL0y47QuqnGwguaMtzKHYbi7jSz5rBx5PJxKD8wthjPXKRyqC1UOwz7fjZ99MT1JdIDbQ/LSkKldb4ouUSU1lU3RVR9kBcQrgMGyR5i3Xi2zw9e6P4r9o1klPECK0vJ3fd03txrAHDuhrqLH6YelQKBgQC/T7owx3iPu01te9RI/Zfft7qSf90JptQkLO/evQnaoQdmp2I6TRbrBAeSierHbj1aQILAYQ6RUqckcupekicth/+ei2lSCnTxTyOpQI9ISzHCWmvjI1M5ZbfCJQ2XKk9mYRMiyHJ1bqN1IsdBj5VVuRprFPPCtoDX0US3c3t4ywKBgQCaRVvL9y1U8mnRH+vnYE/j1k4xc31PRUJagcUb8eJemq8ZgEykKXMysQRpbmIHLsamvGdqFFqTesdthWNw9O2Mg0HUXznmmnlxAwGz/gOT+cx2LQ8aY4zlRmqt6ausP6K8dm+wzdzE78RtigC4MbRF7Y5ETSHLKb1Ohr8Zeo8DvQKBgAvvtIVAnNQS8qTHGhqnv+cUdo6Xbbohb5EGQL0b/FZov6Z3ARj0IF7vdG1/L2fcB/XumnnYVGlax9TtWpQl+E3N83P37M1Sm7NGpcn0njv7fRJMQ/j7BkFJiGqTl0J8QFH58pC0Avgyu/4d+mKry7x6fRx7RS475tQQWYI8sVJ7AoGASAbtR9PXyJxkPOFYA0OkO6TCjkKtrwNt2J/ozZqTvydnV4RZMFNMy6XlHEqJvghvzbPIAz7g0UCA8Gln4eC/zpFAJdarPz54AdYSdsRk6m7jroASQ5+BxxtV6nSi4ljgYhNy81QNB35Jt5sgOrRIsib4Qv74LC6NcoFHAMIauMA=";
    private static final String ENCRYPTION = "A256GCM";
    private static final String ALGORITHM = "RSA-OAEP-256";
    private static final String BOGUS_JWE_WITH_DIFFERENT_ENCRYPTION_METHOD = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIn0.AN1-DLrjDLR_lKBZjmqmSxDBHwKPLdZw1B-j5lUB0NJR-cUwqofrSuFa6iZsi27CNYw7jtm4cby_AXz9siu4bRlQHMHVB0lcKoWwhsr--qn2jYhi-LQ4w2qAH5FW6Btly66dj1SFyW6f_mGkbIA5RUBWpjatgHQLd5QS-W3qe27JIFLR1O-zbCiLcIIpp6_B4pK8xwtatb4fTyUyXvUcr1SamlNF3YxXo0vGNBDW_GpDqp1GjXd2VRc7b1eA21xMNrB-Z2aHDjChPYYNTyZezpCJKx5BY0Jmryfcc2Mq1Y_qDN0fpPVW_hzgIUxu0MDjQPWoWvZtHvdO-bp1FFgWzQ.-uE33rfcCumHKjbRTj1Kkw.0TfKQsSW1ZGsSnAQtztAXg.rrwHSBHYMETeKSG_i-VQsA8pFiWudkq--1q6Ft7u-WM";
    private static final String BOGUS_JWE_WITH_DIFFERENT_ALGORITHM = "eyJhbGciOiJSU0EtT0FFUC0zODQiLCJlbmMiOiJBMjU2R0NNIn0.FZ3zuX36gAtKQIU283Lfnl-iC6SbItEQ58Glk4NaE3LV2HRSDOAYxesQnt647h589XWL-D7WUxkKj5f8svINGaeFctxMdqQdXn3SLYiRHOxEnZdiezTGFGg1jPluQvFrRgToYFCzkk-8RNff9SBrTLTnYbIY3d7bFRyeH-_N5JGffqAFFFS2A1JfaOUWHQzFmXTfrCALTkWh8HNTOTCbBGc0x3oiIOKyuCuRYmhiTFEBlRPMQQaK67T6d8jW4OMMQ0udaYB5QmiLwNDwWmiS3n8nZoX8_bKhgdfpxl0hfuUcVErHx_v8DeKKTNYGeP-XVXmh6itGs1usnR6Z_xPtpg.mNQThHaMrwRaPyqc.bQbD.EJ24m8DEFr41Q_aFIC7A_w";

    @Test
    void checkIfDecryptionWorks() {
        var subject = new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION, PRIVATE_KEY);

        var result = subject.decryptJwe(JWE_WITH_TEST_PASSWORD_STRING);

        assertEquals("testpassword", result);
    }

    @Test
    void checkIfDecryptionFailsWithMalformedData() {
        var subject = new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION, PRIVATE_KEY);

        assertThatExceptionOfType(FormDecryptionFailedException.class)
                .isThrownBy(() -> subject.decryptJwe("MALFORMEDDATA"))
                .withMessage("Couldn't decrypt. Possible source of the issue: JoseException");
    }

    @Test
    void checkIfDecryptionFailsIfProvidedJWEdoesNotMatchTheCriteria() {
        var subject = new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION, PRIVATE_KEY);

        assertThatExceptionOfType(FormDecryptionFailedException.class)
                .isThrownBy(() -> subject.decryptJwe(BOGUS_JWE_WITH_DIFFERENT_ENCRYPTION_METHOD))
                .withMessage("Expected encryption to be A256GCM");
    }

    @Test
    void checkIfDecryptionFailsIfProvidedJWEdoesNotMatchTheAlgorithmCriteria() {
        var subject = new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION, PRIVATE_KEY);

        assertThatExceptionOfType(FormDecryptionFailedException.class)
                .isThrownBy(() -> subject.decryptJwe(BOGUS_JWE_WITH_DIFFERENT_ALGORITHM))
                .withMessage("Expected algorithm to be RSA-OAEP-256");
    }
}