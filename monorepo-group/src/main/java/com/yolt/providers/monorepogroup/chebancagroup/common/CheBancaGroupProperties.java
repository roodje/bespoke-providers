package com.yolt.providers.monorepogroup.chebancagroup.common;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public class CheBancaGroupProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String tokenUrl;
    @NotEmpty
    private String authorizeUrl;
}
