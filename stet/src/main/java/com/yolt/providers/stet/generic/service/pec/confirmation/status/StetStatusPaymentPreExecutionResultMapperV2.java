package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentPreExecutionResultMapperV2;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class StetStatusPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> implements SepaStatusPaymentPreExecutionResultMapper<StetConfirmationPreExecutionResult> {

    private static final int ONLY_ONE_REGION = 0;
    private final AuthenticationMeansSupplier authenticationMeansSupplier;
    private final ProviderIdentification providerIdentification;
    private final SepaTokenPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper;
    private final SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker;
    private final ProviderStateMapper providerStateMapper;
    private final Supplier<String> paymentStatusTemplateSupplier;
    private final HttpClientFactory httpClientFactory;
    private final Function<Region, String> baseUrlFunction;
    private final DefaultProperties properties;

    public StetStatusPaymentPreExecutionResultMapperV2(AuthenticationMeansSupplier authenticationMeansSupplier,
                                                       ProviderIdentification providerIdentification,
                                                       SepaTokenPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                       SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker,
                                                       ProviderStateMapper providerStateMapper,
                                                       HttpClientFactory httpClientFactory,
                                                       Function<Region, String> baseUrlFunction,
                                                       DefaultProperties properties) {
        this.authenticationMeansSupplier = authenticationMeansSupplier;
        this.providerIdentification = providerIdentification;
        this.tokenPaymentPreExecutionResultMapper = tokenPaymentPreExecutionResultMapper;
        this.tokenHttpRequestInvoker = tokenHttpRequestInvoker;
        this.providerStateMapper = providerStateMapper;
        this.paymentStatusTemplateSupplier = () -> "/payment-requests/{paymentRequestResourceId}";
        this.httpClientFactory = httpClientFactory;
        this.baseUrlFunction = baseUrlFunction;
        this.properties = properties;
    }

    @Override
    public StetConfirmationPreExecutionResult map(GetStatusRequest request) {
        DefaultAuthenticationMeans authMeans = authenticationMeansSupplier.getAuthMeans(
                request.getAuthenticationMeans(),
                providerIdentification.getIdentifier());

        Region region = properties.getRegions().get(ONLY_ONE_REGION);

        HttpClient httpClient = httpClientFactory.createHttpClient(
                request.getRestTemplateManager(),
                authMeans,
                baseUrlFunction.apply(region),
                providerIdentification.getDisplayName()
        );

        TokenPaymentPreExecutionResult preExecutionResult = tokenPaymentPreExecutionResultMapper.map(request, authMeans, httpClient, region);
        TokenResponseDTO tokenResponseDTO = tokenHttpRequestInvoker.invokeRequest(preExecutionResult);

        String paymentId = getPaymentIdFromRequestOrProviderState(request);

        return StetConfirmationPreExecutionResult.builder()
                .signer(request.getSigner())
                .authMeans(authMeans)
                .httpClient(httpClient)
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
