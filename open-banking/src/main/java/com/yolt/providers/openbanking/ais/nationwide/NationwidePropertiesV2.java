package com.yolt.providers.openbanking.ais.nationwide;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties("lovebird.nationwide")
@EqualsAndHashCode(callSuper = true)
@Data
@Validated
public class NationwidePropertiesV2 extends DefaultProperties {
    @NotNull
    private String refreshTokenExpiredMessage;

    @NotNull
    protected String registrationUrl;
}