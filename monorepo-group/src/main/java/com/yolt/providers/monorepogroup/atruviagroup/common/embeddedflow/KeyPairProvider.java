package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import lombok.SneakyThrows;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class KeyPairProvider {

    private static final int KEYSIZE = 2048;

    @SneakyThrows
    public KeyPair generateKeypair() {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(KEYSIZE);
        return generator.generateKeyPair();
    }
}
