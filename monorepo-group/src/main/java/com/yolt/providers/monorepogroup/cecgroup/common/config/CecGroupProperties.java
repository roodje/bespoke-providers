package com.yolt.providers.monorepogroup.cecgroup.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class CecGroupProperties {

    @NotEmpty
    private String baseUrl;

    @Positive
    private int paginationLimit;
}