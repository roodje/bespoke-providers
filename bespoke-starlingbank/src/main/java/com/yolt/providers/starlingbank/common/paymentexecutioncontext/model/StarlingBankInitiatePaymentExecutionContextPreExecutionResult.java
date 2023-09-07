package com.yolt.providers.starlingbank.common.paymentexecutioncontext.model;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.starlingbank.common.model.UkDomesticPaymentProviderState;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class StarlingBankInitiatePaymentExecutionContextPreExecutionResult {
    String baseRedirectUrl;
    Map<String, BasicAuthenticationMean> basicAuthMeans;
    String state;
    UkDomesticPaymentProviderState providerStatePayload;
}
