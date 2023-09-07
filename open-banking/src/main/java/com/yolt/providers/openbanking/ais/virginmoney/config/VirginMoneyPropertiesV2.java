package com.yolt.providers.openbanking.ais.virginmoney.config;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@Data
@RequiredArgsConstructor
@ConfigurationProperties("lovebird.virginmoney")
@EqualsAndHashCode(callSuper = true)
public class VirginMoneyPropertiesV2 extends DefaultProperties {

    @NotNull
    private String registrationUrl;

    private String registrationAudience;
}