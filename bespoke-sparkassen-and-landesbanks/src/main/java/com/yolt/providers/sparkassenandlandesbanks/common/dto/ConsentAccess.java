package com.yolt.providers.sparkassenandlandesbanks.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsentAccess {
    private String availableAccountsWithBalance;
}
