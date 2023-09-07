package com.yolt.providers.openbanking.ais.capitalonegroup.capitalone.config;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("lovebird.capitalonegroup.capitalone")
@EqualsAndHashCode(callSuper = true)
public class CapitalOnePropertiesV2 extends DefaultProperties {
}
