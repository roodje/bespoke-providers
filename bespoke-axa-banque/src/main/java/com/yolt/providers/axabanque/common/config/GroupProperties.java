package com.yolt.providers.axabanque.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public class GroupProperties {
    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String authorizationBaseUrl;

    private int transactionPaginationLimit;
}
