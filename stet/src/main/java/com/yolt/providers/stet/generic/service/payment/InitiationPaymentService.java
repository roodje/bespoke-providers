package com.yolt.providers.stet.generic.service.payment;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.payment.PaymentMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import com.yolt.providers.stet.generic.service.payment.rest.PaymentRestClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@RequiredArgsConstructor
public class InitiationPaymentService implements PaymentService {

    private static final String PAYMENT_INITIATION_ENDPOINT = "/payment-requests";
    private static final String PAYMENT_STATUS_TEMPLATE = "/payment-requests/{paymentRequestResourceId}";

    protected final PaymentRestClient restClient;
    protected final PaymentMapper paymentMapper;
    protected final ProviderStateMapper providerStateMapper;
    protected final Scope paymentScope;
    protected final DefaultProperties properties;

    @SneakyThrows
    @Override
    public LoginUrlAndStateDTO initiatePayment(HttpClient httpClient,
                                               InitiatePaymentRequest request,
                                               DefaultAuthenticationMeans authMeans) {
        StetPaymentInitiationRequestDTO paymentInitiationRequestDTO = paymentMapper.mapToStetPaymentInitiationRequestDTO(request, authMeans);
        TokenResponseDTO token = getClientCredentialsToken(httpClient, authMeans, request.getSigner());

        PaymentRequest paymentRequest = new PaymentRequest(
                getPaymentInitiationEndpoint(),
                token.getAccessToken(),
                request.getSigner(),
                request.getPsuIpAddress(),
                authMeans);

        StetPaymentInitiationResponseDTO paymentInitiationResponseDTO = restClient.initiatePayment(
                httpClient, paymentRequest, paymentInitiationRequestDTO);

        String authorizationUrl = paymentInitiationResponseDTO.getConsentApprovalHref();
        if (StringUtils.isEmpty(authorizationUrl)) {
            throw new CreationFailedException("Error when getting the authorization URL from init payment response");
        }
        String paymentRequestResourceId = getPaymentRequestResourceId(authorizationUrl);
        if (StringUtils.isEmpty(paymentRequestResourceId)) {
            throw new CreationFailedException("Missing paymentRequestResourceId");
        }
        String providerState = providerStateMapper.mapToJson(PaymentProviderState.initiatedProviderState(paymentRequestResourceId));
        return new LoginUrlAndStateDTO(authorizationUrl, providerState);
    }

    protected String getPaymentInitiationEndpoint() {
        return PAYMENT_INITIATION_ENDPOINT;
    }

    protected String getPaymentRequestResourceId(String authorizationUrl) {
        return UriComponentsBuilder.fromUriString(authorizationUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get("paymentRequestResourceId");
    }

    @Override
    public SepaPaymentStatusResponseDTO confirmPayment(HttpClient httpClient,
                                                       SubmitPaymentRequest request,
                                                       DefaultAuthenticationMeans authMeans) {
        PaymentProviderState providerState = providerStateMapper.mapToPaymentProviderState(request.getProviderState());

        GetStatusRequest getStatusRequest = new GetStatusRequest(null,
                providerState.getPaymentId(),
                request.getAuthenticationMeans(),
                request.getSigner(),
                request.getRestTemplateManager(),
                request.getPsuIpAddress(),
                request.getAuthenticationMeansReference());

        return getPaymentStatus(httpClient, getStatusRequest, authMeans);
    }

    @Override
    public SepaPaymentStatusResponseDTO getPaymentStatus(HttpClient httpClient,
                                                         GetStatusRequest request,
                                                         DefaultAuthenticationMeans authMeans) {
        String paymentStatusUrl = getPaymentStatusUrl(request.getPaymentId());

        TokenResponseDTO token = getClientCredentialsToken(httpClient, authMeans, request.getSigner());

        PaymentRequest paymentRequest = new PaymentRequest(
                paymentStatusUrl,
                token.getAccessToken(),
                request.getSigner(),
                request.getPsuIpAddress(),
                authMeans);

        StetPaymentStatusResponseDTO paymentStatusResponseDTO = restClient.getPaymentStatus(
                httpClient, paymentRequest);

        StetPaymentStatus paymentStatus = paymentStatusResponseDTO.getPaymentStatus();
        if (Objects.isNull(paymentStatus)) {
            throw new IllegalStateException("Payment status is missing");
        }
        SepaPaymentStatus sepaPaymentStatus = paymentMapper.mapToSepaPaymentStatus(paymentStatus);
        //leaving status call, so it is easier to migrate to PEC
        return new SepaPaymentStatusResponseDTO(request.getPaymentId());
    }

    protected String getPaymentStatusUrl(String paymentId) {
        return UriComponentsBuilder.fromUriString(PAYMENT_STATUS_TEMPLATE)
                .buildAndExpand(paymentId)
                .toUriString();
    }

    @Override
    public TokenResponseDTO getClientCredentialsToken(HttpClient httpClient,
                                                      DefaultAuthenticationMeans authMeans,
                                                      Signer signer) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);
        body.add(OAuth.SCOPE, paymentScope.getValue());

        Region region = properties.getRegions().get(0);
        return restClient.getClientToken(httpClient, region.getTokenUrl(), authMeans, body, signer);
    }
}
