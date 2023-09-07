package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland;

import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.BankOfIrelandGroupProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@Data
@ConfigurationProperties("lovebird.bankofirelandgroup.bankofireland")
@EqualsAndHashCode(callSuper = true)
public class BankOfIrelandProperties extends BankOfIrelandGroupProperties {
}
