package com.yolt.providers.openbanking.ais.cybgroup.clydesdale.config;

import com.yolt.providers.openbanking.ais.cybgroup.common.config.CybgGroupPropertiesV2;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("lovebird.cybgroup.clydesdale")
@EqualsAndHashCode(callSuper = true)
public class ClydesdalePropertiesV2 extends CybgGroupPropertiesV2 {

    private String registrationUrl;

    private String institutionId;
}
