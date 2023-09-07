package com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.config;

import com.yolt.providers.openbanking.ais.virginmoney2group.common.properties.VirginMoney2GroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.virginmoney2group.virginmoney2")
public class VirginMoney2Properties extends VirginMoney2GroupProperties {
}
