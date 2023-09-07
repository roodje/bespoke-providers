package com.yolt.providers.sparkassenandlandesbanks.nordlb;

import com.yolt.providers.sparkassenandlandesbanks.common.config.SparkassenAndLandesbanksProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Data
@Component
@ConfigurationProperties("lovebird.nordlb")
public class NordLbProperties extends SparkassenAndLandesbanksProperties {

    @NotEmpty
    private String bankCode;
}
