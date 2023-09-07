package com.yolt.providers.belfius.belfius.config;

import com.yolt.providers.belfius.common.configuration.BelfiusBaseProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("lovebird.belfiusgroup.belfius")
public class BelfiusProperties extends BelfiusBaseProperties {

}