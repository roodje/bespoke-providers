package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentPreExecutionResultMapper;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Deprecated
public class StetStatusPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> implements SepaStatusPaymentPreExecutionResultMapper<StetConfirmationPreExecutionResult> {

    private final AuthenticationMeansSupplier authenticationMeansSupplier;
    private final ProviderIdentification providerIdentification;
    private final SepaTokenPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper;
    private final SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker;
    private final ProviderStateMapper providerStateMapper;
    private final Supplier<String> paymentStatusTemplateSupplier;

    public StetStatusPaymentPreExecutionResultMapper(AuthenticationMeansSupplier authenticationMeansSupplier,
                                                     ProviderIdentification providerIdentification,
                                                     SepaTokenPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                     SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker,
                                                     ProviderStateMapper providerStateMapper) {
        this.authenticationMeansSupplier = authenticationMeansSupplier;
        this.providerIdentification = providerIdentification;
        this.tokenPaymentPreExecutionResultMapper = tokenPaymentPreExecutionResultMapper;
        this.tokenHttpRequestInvoker = tokenHttpRequestInvoker;
        this.providerStateMapper = providerStateMapper;
        this.paymentStatusTemplateSupplier = () -> "/payment-requests/{paymentRequestResourceId}";
    }

    @Override
    public StetConfirmationPreExecutionResult map(GetStatusRequest request) {
        DefaultAuthenticationMeans authMeans = authenticationMeansSupplier.getAuthMeans(
                request.getAuthenticationMeans(),
                providerIdentification.getIdentifier());

        TokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authMeans);
        TokenResponseDTO tokenResponseDTO = tokenHttpRequestInvoker.invokeRequest(preExecutionResult);

        String paymentId = getPaymentIdFromRequestOrProviderState(request);

        return StetConfirmationPreExecutionResult.builder()
                .signer(request.getSigner())
                .authMeans(authMeans)
                .restTemplateManager(request.getRestTemplateManager())
                .httpMethod(HttpMethod.GET)
                .requestPath(getRequestPath(paymentId))
                .paymentId(paymentId)
                .accessToken(tokenResponseDTO.getAccessToken())
                .psuIpAddress(request.getPsuIpAddress())
                .build();
    }

    protected String getPaymentIdFromRequestOrProviderState(GetStatusRequest request) {
        if (!StringUtils.isEmpty(request.getPaymentId())) {
            return request.getPaymentId();
        }
        PaymentProviderState paymentProviderState = providerStateMapper.mapToPaymentProviderState(request.getProviderState());
        return paymentProviderState.getPaymentId();
    }

    protected String getRequestPath(String paymentId) {
        return UriComponentsBuilder.fromUriString(paymentStatusTemplateSupplier.get())
                .buildAndExpand(paymentId)
                .toUriString();
    }
}
