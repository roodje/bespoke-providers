package com.yolt.providers.axabanque.keytrade;

import com.yolt.providers.axabanque.common.config.GroupProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.axagroup.keytrade")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class KeytradeProperties extends GroupProperties {
}
