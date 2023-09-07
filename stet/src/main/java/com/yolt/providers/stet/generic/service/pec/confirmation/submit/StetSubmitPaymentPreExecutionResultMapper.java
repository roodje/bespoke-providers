package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentPreExecutionResultMapper;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentProviderStateExtractor;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Deprecated
public class StetSubmitPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> implements SepaSubmitPaymentPreExecutionResultMapper<StetConfirmationPreExecutionResult> {

    private final AuthenticationMeansSupplier authenticationMeansSupplier;
    private final StetPaymentProviderStateExtractor<?, ?> providerStateExtractor;
    private final ProviderIdentification providerIdentification;
    private final Supplier<String> paymentConfirmationTemplateSupplier;
    private final SepaTokenPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper;
    private final SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker;

    public StetSubmitPaymentPreExecutionResultMapper(AuthenticationMeansSupplier authenticationMeansSupplier,
                                                     StetPaymentProviderStateExtractor<?, ?> providerStateExtractor,
                                                     ProviderIdentification providerIdentification,
                                                     SepaTokenPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                     SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker) {
        this.authenticationMeansSupplier = authenticationMeansSupplier;
        this.providerStateExtractor = providerStateExtractor;
        this.providerIdentification = providerIdentification;
        this.tokenPaymentPreExecutionResultMapper = tokenPaymentPreExecutionResultMapper;
        this.tokenHttpRequestInvoker = tokenHttpRequestInvoker;
        this.paymentConfirmationTemplateSupplier = () -> "/payment-requests/{paymentRequestResourceId}/confirmation";
    }

    @Override
    public StetConfirmationPreExecutionResult map(SubmitPaymentRequest request) {
        validateRedirectUrlPostedBackFromSite(request.getRedirectUrlPostedBackFromSite());

        DefaultAuthenticationMeans authMeans = authenticationMeansSupplier.getAuthMeans(
                request.getAuthenticationMeans(),
                providerIdentification.getIdentifier());

        TokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authMeans);
        TokenResponseDTO tokenResponseDTO = tokenHttpRequestInvoker.invokeRequest(preExecutionResult);

        String paymentId = providerStateExtractor.mapToPaymentProviderState(request.getProviderState()).getPaymentId();

        return StetConfirmationPreExecutionResult.builder()
                .signer(request.getSigner())
                .authMeans(authMeans)
                .restTemplateManager(request.getRestTemplateManager())
                .redirectUrlPostedBackFromSite(request.getRedirectUrlPostedBackFromSite())
                .httpMethod(HttpMethod.POST)
                .requestPath(getRequestPath(paymentId))
                .paymentId(paymentId)
                .accessToken(tokenResponseDTO.getAccessToken())
                .psuIpAddress(request.getPsuIpAddress())
                .build();
    }

    protected void validateRedirectUrlPostedBackFromSite(String redirectUrlPostedBackFromSite) {
        if (redirectUrlPostedBackFromSite.contains("error")) {
            throw new IllegalStateException("Got error in callback URL. Payment confirmation failed. Redirect url: " + redirectUrlPostedBackFromSite);
        }
    }

    protected String getRequestPath(String paymentId) {
        return UriComponentsBuilder.fromUriString(paymentConfirmationTemplateSupplier.get())
                .buildAndExpand(paymentId)
                .toUriString();
    }
}
