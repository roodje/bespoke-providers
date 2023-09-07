package com.yolt.providers.sparkassenandlandesbanks.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class SparkassenAndLandesbanksProperties {

    @NotEmpty
    private String baseUrl;

    @Positive
    private int paginationLimit;
}
