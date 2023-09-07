package com.yolt.providers.sparkassenandlandesbanks.common.dto;

import com.yolt.providers.sparkassenandlandesbanks.common.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkassenAndLandesbanksProviderState {
    private String codeVerifier;
    private Department department;
    /* It was agreed to keep it as the bank did not want to provide us with list of auth endpoint / token endpoint
     * According to the documentation, during the first call POST /consents returns the WKEP that needs to be called to
     * get abovementioned endpoints.
     */
    private String wellKnownEndpoint;
    private String consentId;
}
