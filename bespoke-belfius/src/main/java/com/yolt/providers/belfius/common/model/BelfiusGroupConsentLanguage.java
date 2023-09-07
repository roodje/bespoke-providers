package com.yolt.providers.belfius.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BelfiusGroupConsentLanguage {

    FR("fr"),
    NL("nl");

    private final String value;
}
