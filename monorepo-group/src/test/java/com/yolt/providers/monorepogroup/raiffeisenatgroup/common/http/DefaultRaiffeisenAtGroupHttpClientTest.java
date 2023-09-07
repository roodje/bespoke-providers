package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.config.RaiffeisenAtGroupProperties;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.*;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.DefaultRaiffeisenAtGroupDateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.RaiffeisenAtGroupDateMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupHttpClientTest {

    private MeterRegistry registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RaiffeisenAtGroupHttpHeadersProducer headersProducer;

    @Mock
    private RaiffeisenAtGroupProperties properties;

    private Clock clock = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));

    private RaiffeisenAtGroupDateMapper dateMapper = new DefaultRaiffeisenAtGroupDateMapper(ZoneId.of("Europe/Vienna"), clock);


    DefaultHttpErrorHandlerV2 errorHandler = new DefaultHttpErrorHandlerV2();

    private DefaultRaiffeisenAtGroupHttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new DefaultRaiffeisenAtGroupHttpClient(registry, restTemplate, "RAIFFEISEN_AT", headersProducer, dateMapper, properties, errorHandler);
    }

    @Test
    void shouldCreateClientCredentialToken() throws TokenInvalidException {
        //given
        var requestHeaders = HttpHeaders.EMPTY;
        var requestBody = new LinkedMultiValueMap<String, String>();
        var tokenResponse = mock(Token.class);
        given(properties.getTokenUrl()).willReturn("https://tokenurl.com");
        given(headersProducer.createClientCredentialTokenHttpHeaders()).willReturn(requestHeaders);
        given(restTemplate.exchange("https://tokenurl.com", HttpMethod.POST, new HttpEntity<>(requestBody, requestHeaders), Token.class))
                .willReturn(new ResponseEntity<Token>(tokenResponse, HttpStatus.OK));

        //when
        var receivedResponse = httpClient.createClientCredentialToken(requestBody);

        //then
        assertThat(receivedResponse).isEqualTo(tokenResponse);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenHttp401IsReceivedFromBank() {
        //given
        var requestHeaders = HttpHeaders.EMPTY;
        var requestBody = new LinkedMultiValueMap<String, String>();
        given(properties.getTokenUrl()).willReturn("https://tokenurl.com");
        given(headersProducer.createClientCredentialTokenHttpHeaders()).willReturn(requestHeaders);
        given(restTemplate.exchange("https://tokenurl.com", HttpMethod.POST, new HttpEntity<>(requestBody, requestHeaders), Token.class))
                .willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.createClientCredentialToken(requestBody);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("We are not authorized to call endpoint: HTTP 401");
    }

    @Test
    void shouldCreateUserConsent() throws TokenInvalidException {
        //given
        var clientToken = "CLIENT-TOKEN";
        var redirectUrl = "https://redirecturl.com";
        var psuIpAddress = "127.0.0.1";
        var requestHeaders = HttpHeaders.EMPTY;
        var requestBody = mock(ConsentRequest.class);
        var consentResponse = mock(CreateConsentResponse.class);
        given(headersProducer.createUserConsentHeaders(clientToken, redirectUrl, psuIpAddress)).willReturn(requestHeaders);
        given(restTemplate.exchange("/v1/consents", HttpMethod.POST, new HttpEntity<>(requestBody, requestHeaders), CreateConsentResponse.class))
                .willReturn(new ResponseEntity<CreateConsentResponse>(consentResponse, HttpStatus.CREATED));

        //when
        var receivedResponse = httpClient.createUserConsent(clientToken, requestBody, redirectUrl, psuIpAddress);

        //then
        assertThat(receivedResponse).isEqualTo(consentResponse);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenHttp403IsReturned() {
        //given
        var clientToken = "CLIENT-TOKEN";
        var redirectUrl = "https://redirecturl.com";
        var psuIpAddress = "127.0.0.1";
        var requestHeaders = HttpHeaders.EMPTY;
        var requestBody = mock(ConsentRequest.class);
        given(headersProducer.createUserConsentHeaders(clientToken, redirectUrl, psuIpAddress)).willReturn(requestHeaders);
        given(restTemplate.exchange("/v1/consents", HttpMethod.POST, new HttpEntity<>(requestBody, requestHeaders), CreateConsentResponse.class))
                .willThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.createUserConsent(clientToken, requestBody, redirectUrl, psuIpAddress);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("Access to call is forbidden: HTTP 403");
    }

    @Test
    void shouldGetUserConsent() throws TokenInvalidException {
        //given
        var clientToken = "THE-CLIENT-TOKEN";
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var requestHeaders = HttpHeaders.EMPTY;
        var getConsentResponse = mock(GetConsentResponse.class);
        given(headersProducer.getUserConsentHeaders(clientToken, psuIpAddress)).willReturn(requestHeaders);
        given(restTemplate.exchange("/v1/consents/{consentId}", HttpMethod.GET, new HttpEntity<>(requestHeaders), GetConsentResponse.class, consentId))
                .willReturn(new ResponseEntity<GetConsentResponse>(getConsentResponse, HttpStatus.OK));

        //when
        var result = httpClient.getConsentStatus(clientToken, consentId, psuIpAddress);

        //then
        assertThat(result).isEqualTo(getConsentResponse);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenHttp403IsReceivedFromGetConsentEndpoint() {
        //given
        var clientToken = "THE-CLIENT-TOKEN";
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var requestHeaders = HttpHeaders.EMPTY;
        var getConsentResponse = mock(GetConsentResponse.class);
        given(headersProducer.getUserConsentHeaders(clientToken, psuIpAddress)).willReturn(requestHeaders);
        given(restTemplate.exchange("/v1/consents/{consentId}", HttpMethod.GET, new HttpEntity<>(requestHeaders), GetConsentResponse.class, consentId))
                .willThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.getConsentStatus(clientToken, consentId, psuIpAddress);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("Access to call is forbidden: HTTP 403");
    }

    @Test
    void shouldDeleteUserConsent() {
        //given
        var clientToken = "THE-CLIENT-TOKEN";
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var requestHeaders = HttpHeaders.EMPTY;
        given(headersProducer.deleteUserConsentHeaders(clientToken, psuIpAddress)).willReturn(requestHeaders);
        given(restTemplate.exchange("/v1/consents/{consentId}", HttpMethod.DELETE, new HttpEntity<>(requestHeaders), Void.class, consentId))
                .willReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.deleteUserConsent(clientToken, consentId, psuIpAddress);

        //then
        assertThatCode(call)
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenHttp401IsReturnDeleteConsentEndpoint() {
        //given
        var clientToken = "THE-CLIENT-TOKEN";
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var requestHeaders = HttpHeaders.EMPTY;
        given(headersProducer.deleteUserConsentHeaders(clientToken, psuIpAddress)).willReturn(requestHeaders);
        given(restTemplate.exchange("/v1/consents/{consentId}", HttpMethod.DELETE, new HttpEntity<>(requestHeaders), Void.class, consentId))
                .willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.deleteUserConsent(clientToken, consentId, psuIpAddress);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("We are not authorized to call endpoint: HTTP 401");
    }

    @Test
    void shouldFetchAccounts() throws TokenInvalidException {
        //given
        var clientAccessToken = "THE-CLIENT-TOKEN";
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var requestHeaders = HttpHeaders.EMPTY;
        given(headersProducer.getFetchDataHeaders(clientAccessToken, consentId, psuIpAddress))
                .willReturn(requestHeaders);
        var expectedAccountResponse = mock(AccountResponse.class);
        given(restTemplate.exchange("/v1/accounts?withBalance=true", HttpMethod.GET, new HttpEntity<>(requestHeaders), AccountResponse.class))
                .willReturn(new ResponseEntity<>(expectedAccountResponse, HttpStatus.OK));

        //when
        var result = httpClient.fetchAccounts(clientAccessToken, consentId, psuIpAddress);

        //then
        assertThat(result).isEqualTo(expectedAccountResponse);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhen401IsReturnedFromAccountEndpoint() {
        //given
        var clientAccessToken = "THE-CLIENT-TOKEN";
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var requestHeaders = HttpHeaders.EMPTY;
        given(headersProducer.getFetchDataHeaders(clientAccessToken, consentId, psuIpAddress))
                .willReturn(requestHeaders);
        given(restTemplate.exchange("/v1/accounts?withBalance=true", HttpMethod.GET, new HttpEntity<>(requestHeaders), AccountResponse.class))
                .willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.fetchAccounts(clientAccessToken, consentId, psuIpAddress);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("We are not authorized to call endpoint: HTTP 401");
    }

    @Test
    void shouldFetchTransactions() throws TokenInvalidException {
        //given
        var clientAccessToken = "THE-CLIENT-TOKEN";
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var accountId = "ACCOUNT-ID";
        var requestHeaders = HttpHeaders.EMPTY;
        var transactionStartFetchTime = Instant.now(clock);
        given(headersProducer.getFetchDataHeaders(clientAccessToken, consentId, psuIpAddress))
                .willReturn(requestHeaders);
        var expectedTransactionResponse = mock(TransactionResponse.class);
        given(restTemplate.exchange("/v1/accounts/{accountId}/transactions?bookingStatus=both&dateFrom=2022-01-01", HttpMethod.GET, new HttpEntity<>(requestHeaders), TransactionResponse.class, accountId))
                .willReturn(new ResponseEntity<>(expectedTransactionResponse, HttpStatus.OK));

        //when
        var result = httpClient.fetchTransaction(accountId, clientAccessToken, consentId, psuIpAddress, transactionStartFetchTime);

        //then
        assertThat(result).isEqualTo(expectedTransactionResponse);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhen403IsReturnedFromTransactionEndpoint() throws TokenInvalidException {
        //given
        var clientAccessToken = "THE-CLIENT-TOKEN";
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var accountId = "ACCOUNT-ID";
        var requestHeaders = HttpHeaders.EMPTY;
        var transactionStartFetchTime = Instant.now(clock);
        given(headersProducer.getFetchDataHeaders(clientAccessToken, consentId, psuIpAddress))
                .willReturn(requestHeaders);
        given(restTemplate.exchange("/v1/accounts/{accountId}/transactions?bookingStatus=both&dateFrom=2022-01-01", HttpMethod.GET, new HttpEntity<>(requestHeaders), TransactionResponse.class, accountId))
                .willThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.fetchTransaction(accountId, clientAccessToken, consentId, psuIpAddress, transactionStartFetchTime);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("Access to call is forbidden: HTTP 403");
    }

    @Test
    void shouldReturnRegistrationResponse() throws TokenInvalidException {
        //given
        var expectedRegistrationResponse = mock(RegistrationResponse.class);
        var requestHeaders = HttpHeaders.EMPTY;
        given(properties.getRegistrationUrl()).willReturn("https://registrationUrl.com");
        given(headersProducer.getRegistrationHeaders())
                .willReturn(requestHeaders);
        given(restTemplate.exchange("https://registrationUrl.com", HttpMethod.POST, new HttpEntity<>(requestHeaders), RegistrationResponse.class))
                .willReturn(new ResponseEntity<>(expectedRegistrationResponse, HttpStatus.CREATED));

        //when
        var result = httpClient.register();

        //then
        assertThat(result).isEqualTo(expectedRegistrationResponse);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhen403IsReturnedFromRegistrationEndpoint() throws TokenInvalidException {
        //given
        var expectedRegistrationResponse = mock(RegistrationResponse.class);
        var requestHeaders = HttpHeaders.EMPTY;
        given(properties.getRegistrationUrl()).willReturn("https://registrationUrl.com");
        given(headersProducer.getRegistrationHeaders())
                .willReturn(requestHeaders);
        given(restTemplate.exchange("https://registrationUrl.com", HttpMethod.POST, new HttpEntity<>(requestHeaders), RegistrationResponse.class))
                .willThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpClient.register();

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("Access to call is forbidden: HTTP 403");
    }
}