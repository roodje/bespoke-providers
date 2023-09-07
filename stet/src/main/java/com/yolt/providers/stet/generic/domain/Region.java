package com.yolt.providers.stet.generic.domain;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public class Region {

    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String authUrl;

    @NotEmpty
    private String tokenUrl;

    private String pisBaseUrl;
}
