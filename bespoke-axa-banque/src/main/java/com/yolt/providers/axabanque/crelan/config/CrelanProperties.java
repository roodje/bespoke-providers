package com.yolt.providers.axabanque.crelan.config;

import com.yolt.providers.axabanque.common.config.GroupProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.axagroup.crelan")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CrelanProperties extends GroupProperties {
}
