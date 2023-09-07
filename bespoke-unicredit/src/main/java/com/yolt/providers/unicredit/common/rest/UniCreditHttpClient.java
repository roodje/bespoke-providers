package com.yolt.providers.unicredit.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.unicredit.common.dto.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class UniCreditHttpClient extends DefaultHttpClient {

    private static final String REGISTER_ENDPOINT = "/tpp/v1/authentications";
    private static final String CONSENT_ENDPOINT = "/v1/consents";
    public static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    @Deprecated
    public static final String TRANSACTION_ENDPOINT = "/v1/accounts/%s/transactions?bookingStatus=both&dateFrom=%s";
    @Deprecated
    public static final String BALANCES_ENDPOINT = "/v1/accounts/%s/balances";
    public static final String BALANCES_ENDPOINT_TEMPLATE = "/v1/accounts/{accountId}/balances";
    public static final String TRANSACTIONS_ENDPOINT_TEMPLATE = "/v1/accounts/{accountId}/transactions";
    private static final String INITIATE_SEPA_PAYMENT_ENDPOINT = "/v1/payments/sepa-credit-transfers";
    private static final String SEPA_PAYMENT_STATUS_TEMPLATE = "/v1/payments/sepa-credit-transfers/{paymentId}/status";

    private final UniCreditHttpHeadersProducer httpHeadersProducer;

    public UniCreditHttpClient(final MeterRegistry registry, final RestTemplate restTemplate, final String providerDisplayName, final UniCreditHttpHeadersProducer httpHeadersProducer) {
        super(registry, restTemplate, providerDisplayName);
        this.httpHeadersProducer = httpHeadersProducer;
    }

    public ResponseEntity<String> register(final RegisterRequestDTO requestDTO) throws TokenInvalidException {
        return exchange(REGISTER_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(requestDTO, httpHeadersProducer.createDefaultHeaders(null)),
                ProviderClientEndpoints.REGISTER,
                String.class);
    }

    public ConsentResponseDTO generateConsent(final ConsentRequestDTO consentRequestDTO,
                                              final String psuIpAddress,
                                              final String redirectUrl,
                                              final String providerIdentifier) throws TokenInvalidException {
        return exchangeForBody(CONSENT_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(consentRequestDTO, httpHeadersProducer.createHeadersForConsent(psuIpAddress, redirectUrl, PSUIDType.forProvider(providerIdentifier))),
                ProviderClientEndpoints.RETRIEVE_ACCOUNT_ACCESS_CONSENT,
                ConsentResponseDTO.class);
    }

    public UniCreditAccountsDTO getAccounts(final String url,
                                            final String consentId,
                                            final String psuIpAddress) throws TokenInvalidException {
        return exchangeForBody(url,
                HttpMethod.GET,
                new HttpEntity<>(httpHeadersProducer.createHeadersForData(psuIpAddress, consentId)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                UniCreditAccountsDTO.class);
    }

    public UniCreditTransactionsDTO getTransactions(final String url,
                                                    final String consentId,
                                                    final String psuIpAddress) throws TokenInvalidException {
        return exchangeForBody(url,
                HttpMethod.GET,
                new HttpEntity<>(httpHeadersProducer.createHeadersForData(psuIpAddress, consentId)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                UniCreditTransactionsDTO.class);
    }

    public UniCreditBalancesDTO getBalances(final String url,
                                            final String consentId,
                                            final String psuIpAddress) throws TokenInvalidException {
        HttpHeaders httpHeaders = httpHeadersProducer.createHeadersForData(psuIpAddress, consentId);

        return exchangeForBody(url,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                UniCreditBalancesDTO.class);
    }

    public UniCreditInitiateSepaPaymentResponseDTO initiateSepaPayment(final UniCreditInitiateSepaPaymentRequestDTO requestDTO,
                                                                       final String psuIpAddress,
                                                                       final String redirectUrl,
                                                                       final String providerIdentifier) throws TokenInvalidException {
        HttpHeaders httpHeaders = httpHeadersProducer.createHeadersForConsent(psuIpAddress, redirectUrl, PSUIDType.forProvider(providerIdentifier));

        return exchangeForBody(INITIATE_SEPA_PAYMENT_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(requestDTO, httpHeaders),
                ProviderClientEndpoints.INITIATE_PAYMENT,
                UniCreditInitiateSepaPaymentResponseDTO.class);
    }

    public UniCreditSepaPaymentStatusResponseDTO getSepaPaymentStatus(final String paymentId,
                                                                      final String psuIpAddress) throws TokenInvalidException {
        HttpHeaders httpHeaders = httpHeadersProducer.createDefaultHeaders(psuIpAddress);

        return exchangeForBody(SEPA_PAYMENT_STATUS_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                ProviderClientEndpoints.SUBMIT_PAYMENT,
                UniCreditSepaPaymentStatusResponseDTO.class,
                paymentId);
    }
}
