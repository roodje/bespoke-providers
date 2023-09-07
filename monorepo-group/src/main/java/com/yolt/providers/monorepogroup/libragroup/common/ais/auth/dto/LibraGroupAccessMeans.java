package com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LibraGroupAccessMeans {
    private LibraGroupTokens tokens;
    private String redirectUrl;
    private String consentId;
}
