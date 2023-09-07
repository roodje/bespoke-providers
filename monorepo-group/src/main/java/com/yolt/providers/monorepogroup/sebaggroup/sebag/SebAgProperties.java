package com.yolt.providers.monorepogroup.sebaggroup.sebag;

import com.yolt.providers.monorepogroup.sebaggroup.common.config.SebAGGroupProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.sebaggroup.sebag")
@Data
public class SebAgProperties extends SebAGGroupProperties {
}
