package com.yolt.providers.stet.generic.exception;

public class ProviderStateMissingRegionException extends RuntimeException {

    public ProviderStateMissingRegionException() {
        super("Region is missing in provider state");
    }
}
