package com.yolt.providers.starlingbank.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedItemDirection {
    IN("IN"),
    OUT("OUT");

    private final String value;
}