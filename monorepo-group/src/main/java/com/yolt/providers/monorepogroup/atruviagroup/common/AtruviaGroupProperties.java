package com.yolt.providers.monorepogroup.atruviagroup.common;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@Validated
public class AtruviaGroupProperties {

    @Positive
    private Integer paginationLimit;

    @NotEmpty
    private List<RegionalBank> regionalBankList;

    public String getBaseUrlByRegionalBankCode(String code){
        return regionalBankList.stream()
                .filter(regionalBank -> regionalBank.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> {
                    throw new IllegalStateException("No regional bank selected! This should never happen!");
                })
                .getBaseUrl();
    }

    @Data
    @Validated
    public static class RegionalBank {

        @NotEmpty
        private String bic;

        @NotEmpty
        private String code;

        @NotEmpty
        private String name;

        @NotEmpty
        private String baseUrl;
    }
}
