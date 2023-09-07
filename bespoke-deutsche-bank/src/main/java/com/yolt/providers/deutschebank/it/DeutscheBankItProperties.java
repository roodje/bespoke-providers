package com.yolt.providers.deutschebank.it;

import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.deutschebankgroup.deutschebankit")
@Data
public class DeutscheBankItProperties extends DeutscheBankGroupProperties {

}
