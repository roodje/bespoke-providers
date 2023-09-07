package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.config;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class LloydsBankingGroupPropertiesV2 extends DefaultProperties {

    private String paymentsUrl;
}


