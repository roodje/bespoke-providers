package com.yolt.providers.openbanking.ais.bankofirelandgroup.common;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BankOfIrelandGroupProperties extends DefaultProperties {

    @NotNull
    private String registrationUrl;
}
