package com.yolt.providers.unicredit.common.service;

import com.yolt.providers.common.exception.*;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapper;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditSepaPaymentTransactionStatusMapper;
import com.yolt.providers.unicredit.common.dto.UniCreditInitiateSepaPaymentRequestDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditInitiateSepaPaymentResponseDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditSepaPaymentStatusResponseDTO;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClient;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import com.yolt.providers.unicredit.it.UniCreditItProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniCreditSepaPaymentServiceV1 implements UniCreditSepaPaymentService {

    private final UniCreditHttpClientFactory httpClientFactory;
    private final UniCreditAuthMeansMapper uniCreditAuthMeansMapper;
    private final UniCreditItProperties properties;
    private final UniCreditSepaPaymentTransactionStatusMapper transactionStatusMapper;

    @Override
    public LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest initiatePaymentRequest, ProviderInfo providerInfo) throws CreationFailedException {
        UniCreditAuthMeans uniCreditAuthMeans = uniCreditAuthMeansMapper.fromBasicAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans(), providerInfo.getIdentifier());
        UniCreditHttpClient httpClient = httpClientFactory.createHttpClient(uniCreditAuthMeans,
                initiatePaymentRequest.getRestTemplateManager(),
                providerInfo.getDisplayName(),
                properties.getBaseUrl());

        try {
            UniCreditInitiateSepaPaymentResponseDTO initiatePaymentResponse = httpClient.initiateSepaPayment(UniCreditInitiateSepaPaymentRequestDTO.from(initiatePaymentRequest.getRequestDTO()),
                    initiatePaymentRequest.getPsuIpAddress(),
                    initiatePaymentRequest.getBaseClientRedirectUrl() + "?state=" + initiatePaymentRequest.getState(),
                    providerInfo.getIdentifier());
            return new LoginUrlAndStateDTO(initiatePaymentResponse.getRedirectUrl(), initiatePaymentResponse.getPaymentId());
        } catch (TokenInvalidException | RuntimeException e) {
            throw new CreationFailedException("Something went wrong while initiating payment.");
        }
    }

    @Override
    public SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest submitPaymentRequest, ProviderInfo providerInfo) throws ConfirmationFailedException {
        UniCreditAuthMeans uniCreditAuthMeans = uniCreditAuthMeansMapper.fromBasicAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans(), providerInfo.getIdentifier());
        UniCreditHttpClient httpClient = httpClientFactory.createHttpClient(uniCreditAuthMeans,
                submitPaymentRequest.getRestTemplateManager(),
                providerInfo.getDisplayName(),
                properties.getBaseUrl());
        return getStatus(httpClient, submitPaymentRequest.getProviderState(), submitPaymentRequest.getPsuIpAddress(), true);
    }

    @Override
    public SepaPaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest, ProviderInfo providerInfo) throws ConfirmationFailedException {
        UniCreditAuthMeans uniCreditAuthMeans = uniCreditAuthMeansMapper.fromBasicAuthenticationMeans(getStatusRequest.getAuthenticationMeans(), providerInfo.getIdentifier());
        UniCreditHttpClient httpClient = httpClientFactory.createHttpClient(uniCreditAuthMeans,
                getStatusRequest.getRestTemplateManager(),
                providerInfo.getDisplayName(),
                properties.getBaseUrl());
        return getStatus(httpClient, getStatusRequest.getPaymentId(), getStatusRequest.getPsuIpAddress(), false);
    }

    private SepaPaymentStatusResponseDTO getStatus(UniCreditHttpClient httpClient,
                                                   String paymentId,
                                                   String psuIpAddress,
                                                   boolean verifyStatusNotCancelled) throws ConfirmationFailedException {
        try {
            UniCreditSepaPaymentStatusResponseDTO sepaPaymentStatusResponse = httpClient.getSepaPaymentStatus(paymentId, psuIpAddress);
            SepaPaymentStatus sepaPaymentStatus = transactionStatusMapper.mapToSepaPaymentStatus(sepaPaymentStatusResponse.getStatus());

            if (verifyStatusNotCancelled && SepaPaymentStatus.REJECTED.equals(sepaPaymentStatus)) {
                throw new PaymentCancelledException("Payment has been cancelled by the PSU. Original payment status: " + sepaPaymentStatusResponse.getStatus());
            }

            return new SepaPaymentStatusResponseDTO(paymentId);
        } catch (TokenInvalidException | ProviderHttpStatusException e) {
            throw new ConfirmationFailedException("Something went wrong while getting payment status.");
        }
    }

}
