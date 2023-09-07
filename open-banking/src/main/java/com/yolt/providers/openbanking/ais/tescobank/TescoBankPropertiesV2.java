package com.yolt.providers.openbanking.ais.tescobank;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties("lovebird.tescobank")
@EqualsAndHashCode(callSuper = true)
public class TescoBankPropertiesV2 extends DefaultProperties {
    @NotNull
    @Getter
    @Setter
    protected String registrationUrl;
}
