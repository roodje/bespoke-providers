package com.yolt.providers.fabric.common.beanconfig;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public class FabricGroupProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String onboardingBaseUrl;
}
