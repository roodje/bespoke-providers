package com.yolt.providers.openbanking.ais.generic2.common;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.util.StringUtils.isEmpty;

public interface EndpointsVersionable {

    String getEndpointsVersion();

    default String getAdjustedUrlPath(final String urlPath) {
        String endpointVersion = getEndpointsVersion();

        try {
            if (isEmpty(endpointVersion) || isEmpty(urlPath) || new URI(urlPath).isAbsolute() || urlPath.startsWith(endpointVersion)) {
                return urlPath;
            }
            return endpointVersion + urlPath;
        } catch (URISyntaxException e) {
            return urlPath;
        }
    }
}
