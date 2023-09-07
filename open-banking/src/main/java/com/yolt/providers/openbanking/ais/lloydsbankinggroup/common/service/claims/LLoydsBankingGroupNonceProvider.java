package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.claims;

/**
 * This is a special class encapsulating business requirement for 'nonce' value
 * that is required for LBG banks.
 * Lloyds supports only first 8 chars from state for nonce
 */
public class LLoydsBankingGroupNonceProvider {

    public String prepareNonce(String state) {
        return state.substring(0, 8);
    }
}
