package com.yolt.providers.openbanking.ais.generic2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionId {
    private String id;
    private boolean generated;
    private int dataHashCode;
    private boolean duplicate;
}
