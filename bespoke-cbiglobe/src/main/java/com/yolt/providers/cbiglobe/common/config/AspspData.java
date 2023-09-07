package com.yolt.providers.cbiglobe.common.config;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class AspspData {

    private String code;
    private String productCode;
    private String displayName;

    public boolean isEmptyProductCode() {
        return !StringUtils.hasText(productCode);
    }
}