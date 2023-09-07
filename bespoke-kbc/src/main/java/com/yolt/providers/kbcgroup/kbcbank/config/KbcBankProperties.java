package com.yolt.providers.kbcgroup.kbcbank.config;

import com.yolt.providers.kbcgroup.common.KbcGroupProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "lovebird.kbcgroup.kbcbank")
@EqualsAndHashCode(callSuper = true)
public class KbcBankProperties extends KbcGroupProperties {

}

