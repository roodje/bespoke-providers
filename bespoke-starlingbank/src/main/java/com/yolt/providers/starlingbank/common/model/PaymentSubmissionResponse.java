package com.yolt.providers.starlingbank.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSubmissionResponse {
    UUID paymentOrderUid;
    ConsentInformation consentInformation;
}
