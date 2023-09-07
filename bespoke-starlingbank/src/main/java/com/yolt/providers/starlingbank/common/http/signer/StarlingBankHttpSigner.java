package com.yolt.providers.starlingbank.common.http.signer;

import java.util.Map;

public interface StarlingBankHttpSigner {

    String createSignature(Map<String, String> headers, String headerKeyId, String httpMethod, String httpEndpoint);
}
