package com.yolt.providers.starlingbank.common.http.signer;

import java.util.Map;

public final class StarlingBankHttpUnsupportedSigner implements StarlingBankHttpSigner {

    @Override
    public String createSignature(Map<String, String> headers, String headerKeyId, String httpMethod, String httpEndpoint) {
        throw new UnsupportedOperationException("Signature creation is not supported");
    }
}
