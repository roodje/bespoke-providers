package com.yolt.providers.monorepogroup.handelsbankengroup.handelsbankennl;

import com.yolt.providers.monorepogroup.handelsbankengroup.common.config.HandelsbankenGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.handelsbankengroup.handelsbankennl")
public class HandelsbankenNlProperties extends HandelsbankenGroupProperties {
}
