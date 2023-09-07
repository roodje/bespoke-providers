package com.yolt.providers.direkt1822group.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentAccess {

    private List<AccountReference> accounts;
    private List<AccountReference> balances;
    private List<AccountReference> transactions;
}
