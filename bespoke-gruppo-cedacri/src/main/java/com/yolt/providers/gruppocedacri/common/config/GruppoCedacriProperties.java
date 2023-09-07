package com.yolt.providers.gruppocedacri.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public abstract class GruppoCedacriProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String tokenUrl;

    @NotEmpty
    private String registrationUrl;

    private int paginationLimit;
}
