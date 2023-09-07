package com.yolt.providers.stet.generic.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DigestAlgorithm {

    MD5("MD5"),
    SHA_1("SHA-1"),
    SHA_256("SHA-256");

    private final String algorithm;
}
