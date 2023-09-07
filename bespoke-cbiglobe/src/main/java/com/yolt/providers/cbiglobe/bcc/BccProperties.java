package com.yolt.providers.cbiglobe.bcc;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@EqualsAndHashCode(callSuper = true)
@Component
@ConfigurationProperties("lovebird.cbiglobe.bcc")
@Data
public class BccProperties extends CbiGlobeBaseProperties {
}
