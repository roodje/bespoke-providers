package com.yolt.providers.openbanking.ais.utils;

public final class FakeKeystoreConstants {

    public static final String FILE_PATH = "certificates/fake/fake-keystore.p12";
    public static final String ALIAS = "1";
    public static final String PASSWORD = "changeit"; //NOSONAR This is a password only used for testing purposes, and is not used outside of the tests

    private FakeKeystoreConstants() {
        // Prevent construction
    }
}
