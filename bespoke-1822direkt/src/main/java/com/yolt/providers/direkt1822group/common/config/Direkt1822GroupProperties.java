package com.yolt.providers.direkt1822group.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class Direkt1822GroupProperties {

    @NotEmpty
    private String baseUrl;

    @Positive
    private int paginationLimit;
}
