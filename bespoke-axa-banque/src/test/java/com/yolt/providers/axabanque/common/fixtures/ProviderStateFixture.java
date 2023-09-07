package com.yolt.providers.axabanque.common.fixtures;

import com.yolt.providers.axabanque.common.model.internal.GroupProviderState;

public class ProviderStateFixture {

    public static GroupProviderState createProviderState(String codeVerifier, String code, String consentId, String traceId, long consentGeneratedAt) {
        return new GroupProviderState(codeVerifier, code, consentId, traceId, consentGeneratedAt);
    }
}
