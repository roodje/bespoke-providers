package com.yolt.providers.openbanking.ais.barclaysgroup.barclays.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Validated
public class CustomerType {

    @NotNull
    private String code;

    @NotNull
    private String type;

    @NotNull
    private String authorizationUrl;


}
