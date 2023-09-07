package com.yolt.providers.triodosbank.common.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@RequiredArgsConstructor
@Validated
public class TriodosBankBaseProperties {

    private String baseUrl;
    private String tokenUrl;
    private int transactionsPaginationLimit;

    public String getCountryPartFromBaseUrl() {
        return baseUrl.substring(baseUrl.lastIndexOf('/'));
    }
}
