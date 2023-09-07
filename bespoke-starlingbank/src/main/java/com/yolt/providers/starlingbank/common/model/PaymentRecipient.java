package com.yolt.providers.starlingbank.common.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRecipient {
    private String payeeName;
    private PayeeType payeeType;
    private String countryCode;
    private String accountIdentifier;
    private String bankIdentifier;
    private BankIdentifierType bankIdentifierType;
}
