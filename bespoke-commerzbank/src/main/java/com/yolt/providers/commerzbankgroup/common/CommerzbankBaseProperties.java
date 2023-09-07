package com.yolt.providers.commerzbankgroup.common;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public abstract class CommerzbankBaseProperties {

    @NotEmpty
    private String baseUrl;

    private int paginationLimit;
}
