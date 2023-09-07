package com.yolt.providers.redsys.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {

    public static String truncateUrlQueryParameters(String url) {
        return UriComponentsBuilder.fromHttpUrl(url)
                .replaceQuery(null)
                .build()
                .toUriString();
    }
}
