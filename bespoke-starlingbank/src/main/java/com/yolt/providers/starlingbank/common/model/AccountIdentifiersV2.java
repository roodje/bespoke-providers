package com.yolt.providers.starlingbank.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountIdentifiersV2 {
    String accountIdentifier;
    String bankIdentifier;
    String iban;
    String bic;
}
