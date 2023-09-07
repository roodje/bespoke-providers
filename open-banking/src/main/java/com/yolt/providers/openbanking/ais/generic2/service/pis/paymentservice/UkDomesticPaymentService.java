package com.yolt.providers.openbanking.ais.generic2.service.pis.paymentservice;

import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPeriodicPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;

/**
 * This interface providers the methods that are required for initiating and confirming payments.
 * This interface is meant to be used with UK (domestic) payments. See {@link DefaultUkDomesticPaymentService} and {@link TestImplUkDomesticPaymentServiceV4}
 * for an example of the implementation fo this interface.
 */
public interface UkDomesticPaymentService {

    /**
     * Create a single domestic UK payment at the provider
     */
    InitiateUkDomesticPaymentResponseDTO createSinglePayment(final HttpClient httpClient,
                                                             final DefaultAuthMeans authenticationMeans,
                                                             final InitiateUkDomesticPaymentRequest request,
                                                             final TokenScope scope) throws CreationFailedException;

    /**
     * Create a scheduled UK payment at the provider
     */
    InitiateUkDomesticPaymentResponseDTO createScheduledPayment(final HttpClient httpClient,
                                                                final DefaultAuthMeans authenticationMeans,
                                                                final InitiateUkDomesticScheduledPaymentRequest request,
                                                                final TokenScope scope) throws CreationFailedException;

    /**
     * Create a scheduled UK payment at the provider
     */
    InitiateUkDomesticPaymentResponseDTO createPeriodicPayment(final HttpClient httpClient,
                                                               final DefaultAuthMeans authenticationMeans,
                                                               final InitiateUkDomesticPeriodicPaymentRequest request,
                                                               final TokenScope scope) throws CreationFailedException;

    /**
     * Retrieve the current status of the payment at the provider
     */
    PaymentStatusResponseDTO getPaymentStatus(final HttpClient httpClient,
                                              final DefaultAuthMeans authenticationMeans,
                                              final GetStatusRequest request,
                                              final TokenScope tokenScope);

    /**
     * Confirm/submit the payment at the provider
     */
    PaymentStatusResponseDTO confirmPayment(final HttpClient httpClient,
                                            final DefaultAuthMeans authenticationMeans,
                                            final SubmitPaymentRequest request,
                                            final TokenScope scope) throws ConfirmationFailedException;
}
