package com.yolt.providers.openbanking.ais.rbsgroup.coutts.service.ais.fetchdataservice;

import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;


public class PortRemovalUrlAdapter implements UrlAdapter {

    @Override
    public String removePortNumberFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return UriComponentsBuilder.fromHttpUrl(url).port(-1).build().toUriString();
    }
}
