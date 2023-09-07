package com.yolt.providers.stet.generic.service.payment;

import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationResponseDTO;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.payment.PaymentMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import com.yolt.providers.stet.generic.service.payment.rest.PaymentRestClient;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
public class InitiationAndConfirmationPaymentService extends InitiationPaymentService {

    private static final String PAYMENT_CONFIRMATION_TEMPLATE = "/payment-requests/{paymentRequestResourceId}/confirmation";

    public InitiationAndConfirmationPaymentService(PaymentRestClient restClient,
                                                   PaymentMapper paymentMapper,
                                                   ProviderStateMapper providerStateMapper,
                                                   Scope paymentScope,
                                                   DefaultProperties properties) {
        super(restClient, paymentMapper, providerStateMapper, paymentScope, properties);
    }


    @SneakyThrows
    @Override
    public SepaPaymentStatusResponseDTO confirmPayment(HttpClient httpClient,
                                                       SubmitPaymentRequest request,
                                                       DefaultAuthenticationMeans authMeans) {
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(request.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap();

        String psuAuthenticationFactor = getPsuAuthenticationFactor(queryParams);
        if (StringUtils.isBlank(psuAuthenticationFactor)) {
            throw new ConfirmationFailedException("Missing psuAuthenticationFactor value in the redirectUrl");
        }
        if (StringUtils.isNotBlank(queryParams.get("error"))) {
            throw new ConfirmationFailedException("Unsuccessful PSU confirmation");
        }
        PaymentProviderState providerState = providerStateMapper.mapToPaymentProviderState(request.getProviderState());
        String paymentId = providerState.getPaymentId();

        StetPaymentConfirmationRequestDTO paymentConfirmationRequestDTO = StetPaymentConfirmationRequestDTO.builder()
                .psuAuthenticationFactor(psuAuthenticationFactor)
                .build();

        String paymentConfirmationUrl = UriComponentsBuilder.fromUriString(PAYMENT_CONFIRMATION_TEMPLATE)
                .buildAndExpand(paymentId)
                .toUriString();

        TokenResponseDTO token = getClientCredentialsToken(httpClient, authMeans, request.getSigner());
        PaymentRequest paymentRequest = new PaymentRequest(
                paymentConfirmationUrl,
                token.getAccessToken(),
                request.getSigner(),
                request.getPsuIpAddress(),
                authMeans);

        StetPaymentConfirmationResponseDTO paymentConfirmationResponseDTO = restClient.confirmPayment(
                httpClient, paymentRequest, paymentConfirmationRequestDTO);

        SepaPaymentStatus sepaPaymentStatus = paymentMapper.mapToSepaPaymentStatus(paymentConfirmationResponseDTO.getPaymentStatus());
        //leaving status call, so it is easier to migrate to PEC
        return new SepaPaymentStatusResponseDTO(paymentId);
    }

    protected String getPsuAuthenticationFactor(Map<String, String> queryParams) {
        return queryParams.get("psuAuthenticationFactor");
    }
}
