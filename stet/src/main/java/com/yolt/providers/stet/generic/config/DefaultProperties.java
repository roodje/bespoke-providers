package com.yolt.providers.stet.generic.config;

import com.yolt.providers.stet.generic.domain.Region;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@Validated
public class DefaultProperties {

    @Positive
    private int paginationLimit;

    private String registrationUrl;

    @Value("${yolt.qseal-certificate-exposure.base-url}")
    private String s3baseUrl;

    @NotEmpty
    public List<Region> regions;

    private long formStepExpiryDurationMillis;

    public Region getRegionByCode(String code) {
        return regions.stream()
                .filter(region -> region.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Region with code '%s' is not found in region list", code)));
    }

    public Region getRegionByBaseUrl(String baseUrl) {
        return regions.stream()
                .filter(region -> region.getBaseUrl().equals(baseUrl))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Region with baseUrl '%s' is not found in region list", baseUrl)));
    }
}
