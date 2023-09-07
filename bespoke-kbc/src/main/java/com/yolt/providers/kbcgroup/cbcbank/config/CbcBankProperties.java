package com.yolt.providers.kbcgroup.cbcbank.config;

import com.yolt.providers.kbcgroup.common.KbcGroupProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "lovebird.kbcgroup.cbcbank")
@EqualsAndHashCode(callSuper = true)
public class CbcBankProperties extends KbcGroupProperties {

}

