package com.yolt.providers.sparkassenandlandesbanks.lbbw;

import com.yolt.providers.sparkassenandlandesbanks.common.config.SparkassenAndLandesbanksProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Data
@Component
@ConfigurationProperties("lovebird.lbbw")
public class LbbwProperties extends SparkassenAndLandesbanksProperties {

    @NotEmpty
    private String bankCode;
}
