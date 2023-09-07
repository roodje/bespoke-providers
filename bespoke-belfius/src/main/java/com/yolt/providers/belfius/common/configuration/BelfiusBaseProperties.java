package com.yolt.providers.belfius.common.configuration;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
@Validated
public class BelfiusBaseProperties {

    @NotEmpty
    private String baseUrl;

    @Min(1)
    private int paginationLimit;

    @NotEmpty
    private String acceptHeaderValue;

    @NotEmpty
    private String transactionsAcceptHeaderValue;
}
