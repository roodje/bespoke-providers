package com.yolt.providers.stet.generic.dto.payment.response;

import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import lombok.Data;

@Data
public class StetPaymentRequest {

    private StetPaymentStatus paymentInformationStatus;
}
