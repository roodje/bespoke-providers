package com.yolt.providers.stet.generic.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Getter
@AllArgsConstructor
public class ExecutionInfo {

    private String url;
    private HttpMethod httpMethod;
    private HttpHeaders httpHeaders;
    private String prometheusPathOverride;
}
