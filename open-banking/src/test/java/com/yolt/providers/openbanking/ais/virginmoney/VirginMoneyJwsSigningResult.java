package com.yolt.providers.openbanking.ais.virginmoney;

import com.yolt.providers.common.cryptography.JwsSigningResult;

public class VirginMoneyJwsSigningResult implements JwsSigningResult {

    private static final String ENCODED_SIGNATURE = "V2hhdCBoYXRoIGdvZCB3cm91Z2h0ID8=..QnkgR2VvcmdlLCBzaGUncyBnb3QgaXQhIEJ5IEdlb3JnZSBzaGUncyBnb3QgaXQhIE5vdyBvbmNlIGFnYWluLCB3aGVyZSBkb2VzIGl0IHJhaW4";

    @Override
    public String getCompactSerialization() {
        return ENCODED_SIGNATURE;
    }

    @Override
    public String getDetachedContentCompactSerialization() {
        return ENCODED_SIGNATURE;
    }
}
