package com.yolt.providers.bancatransilvania.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class BancaTransilvaniaGroupProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String authorizeUrl;

    @Positive
    private int paginationLimit;
}
