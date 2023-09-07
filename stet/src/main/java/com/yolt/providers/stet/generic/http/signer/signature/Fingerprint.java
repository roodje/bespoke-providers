package com.yolt.providers.stet.generic.http.signer.signature;

import org.bouncycastle.crypto.digests.SHA512tDigest;

public class Fingerprint {

    private static final char[] encodingTable = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private final byte[] fingerprint;

    public Fingerprint(byte[] certificate) {
        this.fingerprint = calculateFingerprint(certificate);
    }

    public String toString() {
        var encodedFingerprint = new StringBuilder();

        for (var index = 0; index != this.fingerprint.length; ++index) {
            if (index > 0) {
                encodedFingerprint.append(":");
            }

            encodedFingerprint.append(encodingTable[this.fingerprint[index] >>> 4 & 15]);
            encodedFingerprint.append(encodingTable[this.fingerprint[index] & 15]);
        }

        return encodedFingerprint.toString();
    }

    private byte[] calculateFingerprint(byte[] certificate) {
        var digest = new SHA512tDigest(160);
        digest.update(certificate, 0, certificate.length);
        var fingerprintValue = new byte[digest.getDigestSize()];
        digest.doFinal(fingerprintValue, 0);
        return fingerprintValue;
    }
}
