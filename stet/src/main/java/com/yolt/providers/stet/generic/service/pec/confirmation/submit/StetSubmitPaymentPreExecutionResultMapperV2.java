package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
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
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentProviderStateExtractor;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class StetSubmitPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> implements SepaSubmitPaymentPreExecutionResultMapper<StetConfirmationPreExecutionResult> {

    private static final int ONLY_ONE_REGION = 0;
    private final AuthenticationMeansSupplier authenticationMeansSupplier;
    private final StetPaymentProviderStateExtractor<?, ?> providerStateExtractor;
    private final ProviderIdentification providerIdentification;
    private final SepaTokenPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper;
    private final SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker;
    private final Supplier<String> paymentConfirmationTemplateSupplier;
    private final HttpClientFactory httpClientFactory;
    private final Function<Region, String> baseUrlFunction;
    private final DefaultProperties properties;

    public StetSubmitPaymentPreExecutionResultMapperV2(AuthenticationMeansSupplier authenticationMeansSupplier,
                                                       StetPaymentProviderStateExtractor<?, ?> providerStateExtractor,
                                                       ProviderIdentification providerIdentification,
                                                       SepaTokenPaymentPreExecutionResultMapperV2<TokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                       SepaTokenPaymentHttpRequestInvoker<TokenPaymentPreExecutionResult> tokenHttpRequestInvoker,
                                                       HttpClientFactory httpClientFactory,
                                                       Function<Region, String> baseUrlFunction,
                                                       DefaultProperties properties) {
        this.authenticationMeansSupplier = authenticationMeansSupplier;
        this.providerStateExtractor = providerStateExtractor;
        this.providerIdentification = providerIdentification;
        this.tokenPaymentPreExecutionResultMapper = tokenPaymentPreExecutionResultMapper;
        this.tokenHttpRequestInvoker = tokenHttpRequestInvoker;
        this.paymentConfirmationTemplateSupplier = () -> "/payment-requests/{paymentRequestResourceId}/confirmation";
        this.httpClientFactory = httpClientFactory;
        this.baseUrlFunction = baseUrlFunction;
        this.properties = properties;
    }


    @Override
    public StetConfirmationPreExecutionResult map(SubmitPaymentRequest request) {
        validateRedirectUrlPostedBackFromSite(request.getRedirectUrlPostedBackFromSite());

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

        String paymentId = providerStateExtractor.mapToPaymentProviderState(request.getProviderState()).getPaymentId();

        return StetConfirmationPreExecutionResult.builder()
                .signer(request.getSigner())
                .authMeans(authMeans)
                .httpClient(httpClient)
                .redirectUrlPostedBackFromSite(request.getRedirectUrlPostedBackFromSite())
                .httpMethod(getHttpMethod())
                .requestPath(getRequestPath(paymentId))
                .paymentId(paymentId)
                .accessToken(tokenResponseDTO.getAccessToken())
                .psuIpAddress(request.getPsuIpAddress())
                .build();
    }

    protected HttpMethod getHttpMethod() {
        return HttpMethod.POST;
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
