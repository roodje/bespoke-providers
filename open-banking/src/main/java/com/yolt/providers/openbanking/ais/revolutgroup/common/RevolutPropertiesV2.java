package com.yolt.providers.openbanking.ais.revolutgroup.common;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Data
@Component
@Qualifier("Revolut")
@ConfigurationProperties("lovebird.revolut")
@EqualsAndHashCode(callSuper = true)
public class RevolutPropertiesV2 extends DefaultProperties {

    @NotNull
    private String registrationUrl;

    @NotNull
    private String jwksRootDomain;
}
