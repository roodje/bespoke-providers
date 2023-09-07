package com.yolt.providers.gruppocedacri.common.http;

import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriAccessMeans;
import com.yolt.providers.gruppocedacri.common.config.GruppoCedacriProperties;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentRequest;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentResponse;
import com.yolt.providers.gruppocedacri.common.dto.fetchdata.AccountsResponse;
import com.yolt.providers.gruppocedacri.common.dto.fetchdata.BalancesResponse;
import com.yolt.providers.gruppocedacri.common.dto.fetchdata.TransactionsResponse;
import com.yolt.providers.gruppocedacri.common.dto.registration.AutoOnboardingRequest;
import com.yolt.providers.gruppocedacri.common.dto.registration.AutoOnboardingResponse;
import com.yolt.providers.gruppocedacri.common.dto.token.TokenResponse;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public class GruppoCedacriHttpClientV1 extends DefaultHttpClientV2 implements GruppoCedacriHttpClient {

    private static final String CONSENTS_ENDPOINT = "/v1/consents";
    private static final String DELETE_CONSENT_URL_ENDPOINT = "/v1/consents/{consentId}";
    private static final String GET_ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String GET_ACCOUNT_BALANCES_ENDPOINT = "/v1/accounts/{accountId}/balances";
    private static final String GET_ACCOUNT_TRANSACTIONS_ENDPOINT = "/v1/accounts/{accountId}/transactions?dateFrom={dateFrom}&bookingStatus=booked";

    public static final String TPP_REDIRECT_URI_HEADER_NAME = "tpp-redirect-uri";
    public static final String PSU_IP_ADDRESS_HEADER_NAME = "psu-ip-address";
    private static final String CONSENT_ID_HEADER_NAME = "consent-id";

    private static final DefaultHttpErrorHandlerV2 ERROR_HANDLER = new DefaultHttpErrorHandlerV2();
    private static final GruppoCedacriConsentsErrorHandlerV1 GRUPPO_CEDACRI_CONSENTS_ERROR_HANDLER_V_1 = new GruppoCedacriConsentsErrorHandlerV1();

    private final GruppoCedacriProperties properties;

    public GruppoCedacriHttpClientV1(MeterRegistry registry,
                                     RestTemplate restTemplate,
                                     String provider,
                                     GruppoCedacriProperties properties) {
        super(registry, restTemplate, provider);
        this.properties = properties;
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public AutoOnboardingResponse register(AutoOnboardingRequest autoOnboardingRequest) {
        return exchange(properties.getRegistrationUrl(),
                HttpMethod.POST,
                new HttpEntity<>(autoOnboardingRequest),
                ProviderClientEndpoints.REGISTER,
                AutoOnboardingResponse.class,
                ERROR_HANDLER).getBody();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public TokenResponse getAccessToken(MultiValueMap<String, String> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        return exchange(properties.getTokenUrl(),
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                ProviderClientEndpoints.GET_ACCESS_TOKEN,
                TokenResponse.class,
                ERROR_HANDLER).getBody();
    }

    @Override
    public String getAuthorizationUrl(String authorizationToken, String redirectUrl, String psuIpAddress, ConsentRequest consentRequest) {
        // The only way to obtain authorization URL is to call any PSD2 endpoint with invalid token. As a result 401 error
        // code will be returned with body containing authorization URL. This URL is different each time endpoint is called.
        try {
            createConsent(authorizationToken, redirectUrl, psuIpAddress, consentRequest, GRUPPO_CEDACRI_CONSENTS_ERROR_HANDLER_V_1);
        } catch (HttpStatusCodeException e) {
            return e.getResponseBodyAsString();
        }
        throw new GetLoginInfoUrlFailedException("Missing authorization URL");
    }

    @Override
    public ConsentResponse createConsent(String authorizationToken, String redirectUrl, String psuIpAddress, ConsentRequest consentRequest) {
        return createConsent(authorizationToken, redirectUrl, psuIpAddress, consentRequest, ERROR_HANDLER);
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public void deleteConsent(GruppoCedacriAccessMeans accessMean) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessMean.getTokenResponse().getAccessToken());

        exchange(DELETE_CONSENT_URL_ENDPOINT,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                ERROR_HANDLER,
                accessMean.getConsentId());
    }

    @Override
    public AccountsResponse getAccounts(GruppoCedacriAccessMeans accessMeans, String psuIpAddress) throws TokenInvalidException {
        return exchange(GET_ACCOUNTS_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(getFetchDataHeaders(accessMeans.getConsentId(),
                        accessMeans.getTokenResponse().getAccessToken(),
                        psuIpAddress)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                AccountsResponse.class,
                ERROR_HANDLER
        ).getBody();
    }

    @Override
    public BalancesResponse getBalances(GruppoCedacriAccessMeans accessMeans, String psuIpAddress, String accountId) throws TokenInvalidException {
        return exchange(GET_ACCOUNT_BALANCES_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(getFetchDataHeaders(accessMeans.getConsentId(),
                        accessMeans.getTokenResponse().getAccessToken(),
                        psuIpAddress)),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                BalancesResponse.class,
                ERROR_HANDLER,
                accountId
        ).getBody();
    }

    @Override
    public TransactionsResponse getTransactions(GruppoCedacriAccessMeans accessMeans,
                                                String accountId,
                                                String dateFrom,
                                                String psuIpAddress) throws TokenInvalidException {
        return exchange(GET_ACCOUNT_TRANSACTIONS_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(getFetchDataHeaders(accessMeans.getConsentId(),
                        accessMeans.getTokenResponse().getAccessToken(),
                        psuIpAddress)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionsResponse.class,
                ERROR_HANDLER,
                accountId,
                dateFrom
        ).getBody();
    }

    @Override
    public TransactionsResponse getTransactionsNextPage(String nextPageEndpoint,
                                                        GruppoCedacriAccessMeans accessMeans,
                                                        String psuIpAddress) throws TokenInvalidException {
        return exchange(nextPageEndpoint,
                HttpMethod.GET,
                new HttpEntity<>(getFetchDataHeaders(accessMeans.getConsentId(),
                        accessMeans.getTokenResponse().getAccessToken(),
                        psuIpAddress)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionsResponse.class,
                ERROR_HANDLER
        ).getBody();
    }

    private HttpHeaders getFetchDataHeaders(String consentId, String accessToken, String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(psuIpAddress)) {
            headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        }
        headers.add(CONSENT_ID_HEADER_NAME, consentId);
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private ConsentResponse createConsent(String authorizationToken,
                                          String redirectUrl,
                                          String psuIpAddress,
                                          ConsentRequest consentRequest,
                                          HttpErrorHandlerV2 errorHandler) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authorizationToken);
        headers.add(TPP_REDIRECT_URI_HEADER_NAME, redirectUrl);
        headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);

        try {
            return exchange(CONSENTS_ENDPOINT,
                    HttpMethod.POST,
                    new HttpEntity<>(consentRequest, headers),
                    ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                    ConsentResponse.class,
                    errorHandler).getBody();
        } catch (TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException("Error occurred during consent creation", e);
        }
    }
}