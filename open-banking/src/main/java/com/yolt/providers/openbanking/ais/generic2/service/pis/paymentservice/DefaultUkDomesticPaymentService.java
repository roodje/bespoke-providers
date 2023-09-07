package com.yolt.providers.openbanking.ais.generic2.service.pis.paymentservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.exception.PaymentCancelledException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.UkPaymentMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DefaultUkDomesticPaymentService implements UkDomesticPaymentService, EndpointsVersionable {

    private static final String DOMESTIC_PAYMENTS_PATH = "/pisp/domestic-payment-consents";
    private static final String DOMESTIC_PAYMENTS_SUBMISSION_PATH = "/pisp/domestic-payments";


    private final AuthenticationService authenticationService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final UkPaymentMapper paymentMapper;
    @Getter
    private final String endpointsVersion;

    @Override
    public InitiateUkDomesticPaymentResponseDTO createSinglePayment(final HttpClient httpClient,
                                                                    final DefaultAuthMeans authenticationMeans,
                                                                    final InitiateUkDomesticPaymentRequest request,
                                                                    final TokenScope scope) throws CreationFailedException {
        try {
            AccessMeans clientAccessToken = authenticationService.getClientAccessToken(httpClient, authenticationMeans,
                    request.getAuthenticationMeansReference(), scope, request.getSigner());
            OBWriteDomesticConsent4 requestBody = getCreatePaymentRequestBody(request);

            OBWriteDomesticConsentResponse5 response = restClient.createPayment(httpClient, getAdjustedUrlPath(getDomesticPaymentsPath()),
                    clientAccessToken, authenticationMeans, requestBody, OBWriteDomesticConsentResponse5.class, request.getSigner());

            if (OBWriteDomesticConsentResponse5Data.StatusEnum.REJECTED.equals(response.getData().getStatus())) {
                throw new CreationFailedException("Payment creation failed due to status Rejected");
            }

            String loginUrl = authenticationService.generateAuthorizationUrl(authenticationMeans, response.getData().getConsentId(), request.getState(), request.getBaseClientRedirectUrl(), scope, request.getSigner());
            UkProviderState ukProviderState = new UkProviderState(response.getData().getConsentId(), PaymentType.SINGLE, objectMapper.writeValueAsString(response.getData().getInitiation()));

            return new InitiateUkDomesticPaymentResponseDTO(loginUrl, objectMapper.writeValueAsString(ukProviderState));
        } catch (TokenInvalidException e) {
            throw new CreationFailedException("Invalid OAuth2 token.", e);
        } catch (HttpStatusCodeException e) {
            String msg = String.format("Unable to create payment (intent). Received error code %s", e.getStatusCode());
            throw new CreationFailedException(msg, e);
        } catch (JsonProcessingException e) {
            throw new CreationFailedException("Error processing the json of the initiation message");
        } catch (IllegalArgumentException e) {
            throw new CreationFailedException(e);
        }
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO createScheduledPayment(HttpClient httpClient, DefaultAuthMeans authenticationMeans, InitiateUkDomesticScheduledPaymentRequest request, TokenScope scope) {
        throw new NotImplementedException();
    }

    @Override
    public InitiateUkDomesticPaymentResponseDTO createPeriodicPayment(HttpClient httpClient, DefaultAuthMeans authenticationMeans, InitiateUkDomesticPeriodicPaymentRequest request, TokenScope scope) {
        throw new NotImplementedException();
    }

    @Override
    public PaymentStatusResponseDTO getPaymentStatus(HttpClient httpClient, DefaultAuthMeans authenticationMeans, GetStatusRequest request, TokenScope tokenScope) {
        throw new NotImplementedException();
    }

    @Override
    public PaymentStatusResponseDTO confirmPayment(final HttpClient httpClient,
                                                   final DefaultAuthMeans authenticationMeans,
                                                   final SubmitPaymentRequest request,
                                                   final TokenScope scope) throws ConfirmationFailedException {
        try {
            AccessMeans userAccessToken = retrieveUserAccessToken(httpClient, authenticationMeans, request.getRedirectUrlPostedBackFromSite(),
                    scope, request.getSigner());
            OBWriteDomestic2 requestBody = getConfirmPaymentRequestBody(request);

            OBWriteDomesticResponse5 response = restClient.submitPayment(httpClient, getAdjustedUrlPath(getDomesticPaymentsSubmissionPath()),
                    userAccessToken, authenticationMeans, requestBody, OBWriteDomesticResponse5.class, request.getSigner());

            if (OBWriteDomesticResponse5Data.StatusEnum.REJECTED.equals(response.getData().getStatus())) {
                throw new ConfirmationFailedException("Payment submission failed due to status Rejected");
            }

            return new PaymentStatusResponseDTO(request.getProviderState(), response.getData().getDomesticPaymentId());
        } catch (TokenInvalidException e) {
            throw new ConfirmationFailedException("Invalid OAuth2 token.", e);
        } catch (HttpStatusCodeException e) {
            String msg = String.format("Unable to submit payment. Received error code %s", e.getStatusCode());
            throw new ConfirmationFailedException(msg, e);
        } catch (JsonProcessingException e) {
            throw new ConfirmationFailedException("Error processing the json of the confirmation message");
        } catch (IllegalArgumentException e) {
            throw new ConfirmationFailedException(e);
        }
    }

    protected AccessMeans retrieveUserAccessToken(final HttpClient httpClient,
                                                  final DefaultAuthMeans authenticationMeans,
                                                  final String redirectUrl,
                                                  final TokenScope scope,
                                                  final Signer signer) throws ConfirmationFailedException {
        final UriComponents uriComponents = UriComponentsBuilder
                .fromUriString(redirectUrl)
                .build();

        final Map<String, String> queryAndFragmentParameters = uriComponents
                .getQueryParams()
                .toSingleValueMap();

        if (StringUtils.isNotEmpty(uriComponents.getFragment())) {
            queryAndFragmentParameters.putAll(getMapFromFragmentString(uriComponents.getFragment()));
        }

        if (queryAndFragmentParameters.containsKey("error")) {
            String error = queryAndFragmentParameters.get("error");
            if ("access_denied".equals(error)) {
                throw new PaymentCancelledException("Got error in redirect URL: " + error);
            } else {
                throw new ConfirmationFailedException("Got error in redirect URL: " + error);
            }
        }

        String authorizationCode = queryAndFragmentParameters.get("code");

        int queryParamStartIndex = redirectUrl.indexOf('?');
        if (queryParamStartIndex == -1) {
            queryParamStartIndex = redirectUrl.indexOf('#');
        }
        String cleanRedirectUrl = redirectUrl;
        if (queryParamStartIndex > -1) {
            cleanRedirectUrl = redirectUrl.substring(0, queryParamStartIndex);
        }

        try {
            return authenticationService.createAccessToken(httpClient, authenticationMeans, null, authorizationCode, cleanRedirectUrl, scope, signer);
        } catch (TokenInvalidException e) {
            throw new ConfirmationFailedException("Failed to create payments AccessToken", e);
        }
    }

    protected OBWriteDomesticConsent4 getCreatePaymentRequestBody(final InitiateUkDomesticPaymentRequest request) {
        return paymentMapper.mapToSetupRequest(request);
    }

    protected OBWriteDomestic2 getConfirmPaymentRequestBody(final SubmitPaymentRequest request) throws JsonProcessingException {
        UkProviderState providerState = objectMapper.readValue(request.getProviderState(), UkProviderState.class);
        OBWriteDomestic2DataInitiation originalInitiation = readProviderState(providerState.getOpenBankingPayment());
        return paymentMapper.mapToSubmitRequest(providerState.getConsentId(), originalInitiation);
    }

    private OBWriteDomestic2DataInitiation readProviderState(Object openBankingPayment) throws JsonProcessingException {
        if (openBankingPayment == null) {
            return null;
        }
        return objectMapper.readValue((String) openBankingPayment, OBWriteDomestic2DataInitiation.class);
    }

    protected Map<String, String> getMapFromFragmentString(String queryString) {
        String[] queryParams = queryString.split("&");
        Map<String, String> mappedQueryParams = new HashMap<>();
        for (String queryParam : queryParams) {
            String[] keyValue = queryParam.split("=");
            String value = keyValue.length == 2 ? keyValue[1] : null;
            mappedQueryParams.put(keyValue[0], value);
        }
        return mappedQueryParams;
    }

    protected String getDomesticPaymentsPath() {
        return DOMESTIC_PAYMENTS_PATH;
    }

    protected String getDomesticPaymentsSubmissionPath() {
        return DOMESTIC_PAYMENTS_SUBMISSION_PATH;
    }
}
