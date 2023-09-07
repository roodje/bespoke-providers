package com.yolt.providers.brdgroup.common.authorization;

import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClient;

public interface BrdGroupConsentStatusValidator {

    void validate(BrdGroupHttpClient httpClient, String consentId);
}
