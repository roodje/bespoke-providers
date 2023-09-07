package com.yolt.providers.openbanking.ais.sainsburys;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Component
@Validated
@EqualsAndHashCode
@ConfigurationProperties("lovebird.sainsburys")
public class SainsburysPropertiesV2 extends DefaultProperties {
    @NotNull
    @Getter
    @Setter
    private String registrationUrl;
}



