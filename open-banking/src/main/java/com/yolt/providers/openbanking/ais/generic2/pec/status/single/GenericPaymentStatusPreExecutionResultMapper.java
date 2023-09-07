package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkDomesticStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAccessTokenProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class GenericPaymentStatusPreExecutionResultMapper implements UkDomesticStatusPaymentPreExecutionResultMapper<GenericPaymentStatusPreExecutionResult> {

    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final GenericPaymentAccessTokenProvider paymentAccessTokenProvider;
    private final UkProviderStateDeserializer ukProviderStateDeserializer;

    @Override
    public GenericPaymentStatusPreExecutionResult map(GetStatusRequest getStatusRequest) {
        String paymentId = getStatusRequest.getPaymentId();
        String consentId = extractConsentId(getStatusRequest);

        DefaultAuthMeans authenticationMeans = getAuthenticationMeans.apply(getStatusRequest.getAuthenticationMeans());
        RestTemplateManager restTemplateManager = getStatusRequest.getRestTemplateManager();
        Signer signer = getStatusRequest.getSigner();

        AccessMeans accessMeans = paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, authenticationMeans, getStatusRequest.getAuthenticationMeansReference(), signer);

        return new GenericPaymentStatusPreExecutionResult(accessMeans.getAccessToken(),
                authenticationMeans,
                restTemplateManager,
                paymentId,
                consentId);
    }

    private String extractConsentId(GetStatusRequest getStatusRequest) {
        return Optional.ofNullable(getStatusRequest.getProviderState())
                .filter(providerState -> !StringUtils.isEmpty(providerState))
                .map(ukProviderStateDeserializer::deserialize)
                .map(UkProviderState::getConsentId)
                .orElse("");
    }
}
