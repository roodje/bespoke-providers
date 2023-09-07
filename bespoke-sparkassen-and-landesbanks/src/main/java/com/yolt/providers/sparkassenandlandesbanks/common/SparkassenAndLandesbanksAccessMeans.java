package com.yolt.providers.sparkassenandlandesbanks.common;

import lombok.Data;

@Data
public class SparkassenAndLandesbanksAccessMeans {
    private final String accessToken;
    private final String refreshToken;
    private final String consentId;
    private final Department department;
    /* It was agreed to keep it as the bank did not want to provide us with list of auth endpoint / token endpoint
     * According to the documentation, during the first call POST /consents returns the WKEP that needs to be called to
     * get abovementioned endpoints.
     */
    private final String wellKnownEndpoint;
}
