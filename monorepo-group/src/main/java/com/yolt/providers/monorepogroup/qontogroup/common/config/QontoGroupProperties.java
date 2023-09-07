package com.yolt.providers.monorepogroup.qontogroup.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class QontoGroupProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String tokenUrl;
    @NotEmpty
    private String authorizationUrl;
    @Positive
    private int paginationLimit;
    @Value("${yolt.qseal-certificate-exposure.base-url}")
    private String s3baseUrl;
}
