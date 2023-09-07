package com.yolt.providers.kbcgroup.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsentAccess {
    private List<AccountReference> balances;
    private List<AccountReference> transactions;
}
