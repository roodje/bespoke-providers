package com.yolt.providers.consorsbankgroup.common.ais;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public abstract class DefaultProperties {

    @NotEmpty
    private String baseUrl;

}
