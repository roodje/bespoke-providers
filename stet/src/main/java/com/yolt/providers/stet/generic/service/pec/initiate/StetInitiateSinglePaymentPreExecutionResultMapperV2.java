package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentPreExecutionResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Deprecated
public class StetInitiateSinglePaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> implements SepaInitiateSinglePaymentPreExecutionResultMapper<StetInitiatePreExecutionResult> {

    private final AuthenticationMeansSupplier authenticationMeansSupplier;
    private final ProviderIdentification providerIdentification;
    private final SepaTokenPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper;
    private final SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker;
    private final Supplier<String> paymentInitiationEndpointSupplier;

    public StetInitiateSinglePaymentPreExecutionResultMapperV2(AuthenticationMeansSupplier authenticationMeansSupplier,
                                                               ProviderIdentification providerIdentification,
                                                               SepaTokenPaymentPreExecutionResultMapper<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                               SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker) {
        this.authenticationMeansSupplier = authenticationMeansSupplier;
        this.providerIdentification = providerIdentification;
        this.tokenPaymentPreExecutionResultMapper = tokenPaymentPreExecutionResultMapper;
        this.tokenHttpRequestInvoker = tokenHttpRequestInvoker;
        this.paymentInitiationEndpointSupplier = () -> "/payment-requests";
    }

    @Override
    public StetInitiatePreExecutionResult map(InitiatePaymentRequest request) {
        DefaultAuthenticationMeans authMeans = authenticationMeansSupplier.getAuthMeans(
                request.getAuthenticationMeans(),
                providerIdentification.getIdentifier());

        TokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authMeans);
        TokenResponseDTO tokenResponseDTO = tokenHttpRequestInvoker.invokeRequest(preExecutionResult);

        return StetInitiatePreExecutionResult.builder()
                .signer(request.getSigner())
                .authMeans(authMeans)
                .restTemplateManager(request.getRestTemplateManager())
                .state(request.getState())
                .baseClientRedirectUrl(request.getBaseClientRedirectUrl())
                .httpMethod(HttpMethod.POST)
                .requestPath(paymentInitiationEndpointSupplier.get())
                .accessToken(tokenResponseDTO.getAccessToken())
                .psuIpAddress(request.getPsuIpAddress())
                .sepaRequestDTO(request.getRequestDTO())
                .build();
    }
}
