package com.yolt.providers.openbanking.ais.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@UtilityClass
public class UriHelper {

    public Map<String, String> extractQueryParams(String uri) {
        return UriComponentsBuilder.fromUriString(uri).build()
                .getQueryParams()
                .toSingleValueMap();
    }
}
