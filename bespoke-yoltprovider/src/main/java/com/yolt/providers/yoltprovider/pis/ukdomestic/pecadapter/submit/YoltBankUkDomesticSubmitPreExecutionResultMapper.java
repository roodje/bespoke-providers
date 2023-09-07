package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkDomesticSubmitPreExecutionResultMapper;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.Scenario;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
public class YoltBankUkDomesticSubmitPreExecutionResultMapper implements UkDomesticSubmitPreExecutionResultMapper<YoltBankUkSubmitPreExecutionResult> {

    private static final String PAYMENT_ID = "payment_id";
    private final ObjectMapper objectMapper;

    @Override
    public YoltBankUkSubmitPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) {
        String redirectUrlPostedBackFromSite = submitPaymentRequest.getRedirectUrlPostedBackFromSite();
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams();
        MultiValueMap<String, String> fragmentParams = Scenario.fragmentMap(redirectUrlPostedBackFromSite)
                .orElse(new LinkedMultiValueMap<>()); //N.B. this map does not support allAll() or computeIfAbsent()
        String paymentId = params.getOrDefault(PAYMENT_ID, fragmentParams.get(PAYMENT_ID)).get(0);
        return new YoltBankUkSubmitPreExecutionResult(
                UUID.fromString(paymentId),
                PaymentAuthenticationMeans.fromAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans()).getClientId(),
                retrievePaymentType(submitPaymentRequest.getProviderState())
        );
    }

    private PaymentType retrievePaymentType(String providerState) {
        try {
            return objectMapper.readValue(providerState, UkProviderState.class).getPaymentType();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not find payment type in providerState during payment submission");
        }
    }
}
