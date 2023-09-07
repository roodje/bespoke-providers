package com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkDomesticSubmitPreExecutionResultMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthMeansSupplier;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClientFactoryV4;
import com.yolt.providers.starlingbank.common.model.AccountV2;
import com.yolt.providers.starlingbank.common.model.AccountsResponseV2;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.MalformedProviderStateException;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.StarlingBankHttpHeadersProducerFactory;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import com.yolt.providers.starlingbank.common.service.authorization.StarlingBankAuthorizationService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StarlingBankSubmitPaymentPreExecutorResultMapper implements UkDomesticSubmitPreExecutionResultMapper<StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    private static final String ACCOUNTS_URL = "/api/v2/accounts";
    private static final String PAYMENTS_URL_TEMPLATE = "/api/v2/payments/local/account/%s/category/%s";

    private final StarlingBankAuthorizationService authorizationService;
    private final ObjectMapper objectMapper;
    private final StarlingBankHttpHeadersProducerFactory httpHeadersProducerFactory;
    private final StarlingBankHttpClientFactoryV4 httpClientFactory;
    private final StarlingBankAuthMeansSupplier authMeansSupplier;
    private final String providerIdentifierDisplayName;
    private final String providerIdentifier;
    private final Clock clock;

    @Override
    public StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) throws PaymentExecutionTechnicalException {
        StarlingBankAuthenticationMeans authMeans = authMeansSupplier.createAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans(), providerIdentifier);
        StarlingBankHttpClient httpClient = httpClientFactory.createHttpClient(
                submitPaymentRequest.getRestTemplateManager(),
                providerIdentifierDisplayName,
                httpHeadersProducerFactory.createHeadersProducer(authMeans, submitPaymentRequest.getSigner()),
                authMeans);

        UkProviderState state;
        InitiateUkDomesticPaymentRequestDTO payload;
        try {
            state = objectMapper.readValue(submitPaymentRequest.getProviderState(), UkProviderState.class);
            payload = objectMapper.convertValue(state.getOpenBankingPayment(), InitiateUkDomesticPaymentRequestDTO.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new MalformedProviderStateException("Unable to parse provider state for submit" );
        }

        Token oAuthToken = authorizationService.getOAuthToken(httpClient, authMeans, submitPaymentRequest.getRedirectUrlPostedBackFromSite());
        String url = createSubmitUrl(httpClient, oAuthToken.getAccessToken(), extractCurrencyCode(payload));

        return StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult.builder()
                .redirectUrlPostedBackFromSite(submitPaymentRequest.getRedirectUrlPostedBackFromSite())
                .expiresIn(Date.from(Instant.now(clock).plusSeconds(oAuthToken.getExpiresIn())))
                .refreshToken(oAuthToken.getRefreshToken())
                .token(oAuthToken.getAccessToken())
                .signer(submitPaymentRequest.getSigner())
                .paymentRequest(payload)
                .httpClient(httpClient)
                .url(url)
                .authenticationMeans(authMeans)
                .build();
    }

    private String createSubmitUrl(StarlingBankHttpClient httpClient, String accessToken, CurrencyCode currencyCode) {
        AccountsResponseV2 accountsResponse;
        try {
            accountsResponse = httpClient.fetchAccounts(ACCOUNTS_URL, accessToken);
            AccountV2 account = getSourceAccount(accountsResponse.getAccounts(), currencyCode);
            return String.format(PAYMENTS_URL_TEMPLATE, account.getAccountUid(), account.getDefaultCategory());
        } catch (TokenInvalidException e) {
            throw PaymentExecutionTechnicalException.paymentSubmissionException(e);
        }
    }

    private AccountV2 getSourceAccount(final List<AccountV2> accounts, CurrencyCode currencyCode) {
        List<AccountV2> accountsWithMatchingCurrency = accounts.stream()
                .filter(account -> currencyCode.toString().equalsIgnoreCase(account.getCurrency()))
                .collect(Collectors.toList());

        if (accountsWithMatchingCurrency.size() == 1) {
            return accountsWithMatchingCurrency.get(0);
        }
        throw PaymentExecutionTechnicalException.paymentSubmissionException(new IllegalArgumentException("Cannot distinguish which account should be payment source"));
    }

    private CurrencyCode extractCurrencyCode(InitiateUkDomesticPaymentRequestDTO request) {
        return CurrencyCode.valueOf(request.getCurrencyCode());
    }
}
