package com.yolt.providers.stet.lclgroup.lcl.configuration;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("LclStetProperties")
@ConfigurationProperties("lovebird.stet.lclgroup.lcl")
public class LclStetProperties extends DefaultProperties {
}
