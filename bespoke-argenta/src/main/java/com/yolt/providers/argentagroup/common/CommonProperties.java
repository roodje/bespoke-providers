package com.yolt.providers.argentagroup.common;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Validated
public abstract class CommonProperties {

    @NotEmpty
    private String baseUrl;

    @NotNull
    @Positive
    private Integer paginationLimit;

}
