package com.yolt.providers.direkt1822group.direkt1822.config;

import com.yolt.providers.direkt1822group.common.config.Direkt1822GroupProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.direkt1822group.direkt1822")
@Data
public class Direkt1822Properties extends Direkt1822GroupProperties {
}
