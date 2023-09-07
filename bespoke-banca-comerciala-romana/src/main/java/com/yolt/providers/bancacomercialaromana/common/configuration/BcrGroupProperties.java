package com.yolt.providers.bancacomercialaromana.common.configuration;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public abstract class BcrGroupProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String authorizationBaseUrl;

    @NotEmpty
    private String tokenUrl;

    @Positive
    private Integer paginationLimit;
}
