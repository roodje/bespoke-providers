package com.yolt.providers.brdgroup.brd;

import com.yolt.providers.brdgroup.common.config.BrdGroupProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.brdgroup.brd")
@Data
public class BrdProperties extends BrdGroupProperties {
}
