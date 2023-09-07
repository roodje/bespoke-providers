package com.yolt.providers.deutschebank.common.domain.model.consent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Access {

    private String allPsd2;
    private List<String> balances;
    private List<String> transactions;
}
