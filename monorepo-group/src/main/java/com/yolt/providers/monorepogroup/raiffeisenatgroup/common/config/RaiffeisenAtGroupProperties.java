package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class RaiffeisenAtGroupProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String tokenUrl;
    @NotEmpty
    private String registrationUrl;
    @Positive
    private int paginationLimit;
}
