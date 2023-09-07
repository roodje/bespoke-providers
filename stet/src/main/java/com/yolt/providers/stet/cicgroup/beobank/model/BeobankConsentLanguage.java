package com.yolt.providers.stet.cicgroup.beobank.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BeobankConsentLanguage {

    EN("en"),
    FR("fr"),
    NL("nl");

    private final String value;
}