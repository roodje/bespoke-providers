package com.yolt.providers.n26.n26.config;

import com.yolt.providers.n26.common.config.BaseN26Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("lovebird.n26group.n26")
public class N26Properties extends BaseN26Properties {

}