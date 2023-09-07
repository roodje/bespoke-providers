package com.yolt.providers.bunq.common.model;

import lombok.Value;

import java.util.List;

@Value
public class PaymentServiceProviderDraftPaymentResponse {

    private List<IdResponse> Response;

}
