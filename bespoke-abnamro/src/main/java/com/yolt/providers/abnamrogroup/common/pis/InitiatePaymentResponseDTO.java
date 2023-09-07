package com.yolt.providers.abnamrogroup.common.pis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiatePaymentResponseDTO {

    private String accountNumber;
    private String transactionId;
    private String status;
    private String accountHolderName;
    private String redirectUri;
}
