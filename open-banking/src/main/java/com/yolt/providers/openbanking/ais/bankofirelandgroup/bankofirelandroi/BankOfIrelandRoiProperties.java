package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofirelandroi;

import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.BankOfIrelandGroupProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@Data
@ConfigurationProperties("lovebird.bankofirelandgroup.bankofirelandroi")
@EqualsAndHashCode(callSuper = true)
public class BankOfIrelandRoiProperties extends BankOfIrelandGroupProperties {
}
