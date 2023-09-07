package com.yolt.providers.knabgroup.knab.config;

import com.yolt.providers.knabgroup.common.configuration.KnabGroupProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.knabgroup.knab")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class KnabProperties extends KnabGroupProperties {

    public KnabProperties() {
        super();
    }
}