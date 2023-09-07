package com.yolt.providers.abnamrogroup.abnamro;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "lovebird.abnamrogroup.abnamro")
public class AbnAmroProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String oauth2Url;

    @NotEmpty
    private String tokenUrl;

    @NotEmpty
    private String aisScope;

    private int paginationLimit;
}
