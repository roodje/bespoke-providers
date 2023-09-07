package com.yolt.providers.triodosbank.nl;

import com.yolt.providers.triodosbank.common.config.TriodosBankBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.triodosbank.nl")
public class TriodosBankNLProperties extends TriodosBankBaseProperties {
}
