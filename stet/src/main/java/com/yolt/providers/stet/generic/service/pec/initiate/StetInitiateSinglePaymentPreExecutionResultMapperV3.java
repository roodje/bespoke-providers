package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentPreExecutionResultMapperV2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class StetInitiateSinglePaymentPreExecutionResultMapperV3<TokenPaymentPreExecutionResult> implements SepaInitiateSinglePaymentPreExecutionResultMapper<StetInitiatePreExecutionResult> {

    private static final int ONLY_ONE_REGION = 0;
    private final AuthenticationMeansSupplier authenticationMeansSupplier;
    private final ProviderIdentification providerIdentification;
    private final SepaTokenPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper;
    private final SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker;
    private final Supplier<String> paymentInitiationEndpointSupplier;
    private final HttpClientFactory httpClientFactory;
    private final Function<Region, String> baseUrlFunction;
    private final DefaultProperties properties;

    public StetInitiateSinglePaymentPreExecutionResultMapperV3(AuthenticationMeansSupplier authenticationMeansSupplier,
                                                               ProviderIdentification providerIdentification,
                                                               SepaTokenPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                               SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker,
                                                               HttpClientFactory httpClientFactory,
                                                               Function<Region, String> baseUrlFunction,
                                                               DefaultProperties properties) {
        this.authenticationMeansSupplier = authenticationMeansSupplier;
        this.providerIdentification = providerIdentification;
        this.tokenPaymentPreExecutionResultMapper = tokenPaymentPreExecutionResultMapper;
        this.tokenHttpRequestInvoker = tokenHttpRequestInvoker;
        this.httpClientFactory = httpClientFactory;
        this.baseUrlFunction = baseUrlFunction;
        this.paymentInitiationEndpointSupplier = () -> "/payment-requests";
        this.properties = properties;
    }

    @Override
    public StetInitiatePreExecutionResult map(InitiatePaymentRequest request) {
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

        return StetInitiatePreExecutionResult.builder()
                .signer(request.getSigner())
                .authMeans(authMeans)
                .httpClient(httpClient)
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
