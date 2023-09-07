package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.ConsentRequest;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.ConsentStatus;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.CreateConsentResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.GetConsentResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.RaiffeisenAtGroupProviderState;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.DefaultRaiffeisenAtGroupHttpClient;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.DefaultRaiffeisenAtGroupDateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.DefaultRaiffeisenAtGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.ProviderStateProcessingException;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupAuthorizationServiceTest {

    @Mock
    private DefaultRaiffeisenAtGroupTokenService tokenService;
    @Mock
    private DefaultRaiffeisenAtGroupProviderStateMapper providerStateMapper;
    private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private DefaultRaiffeisenAtGroupDateMapper dateMapper = new DefaultRaiffeisenAtGroupDateMapper(ZoneId.of("Europe/Vienna"), clock);

    private DefaultRaiffeisenAtGroupAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new DefaultRaiffeisenAtGroupAuthorizationService(tokenService, providerStateMapper, dateMapper, clock);
    }

    @Test
    void shouldReturnRedirectStepWithUrl() throws TokenInvalidException, ProviderStateProcessingException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var redirectUrl = "https://baseredirecturl.com";
        var psuIpAddress = "127.0.0.1";
        var state = UUID.randomUUID().toString();
        var clientCredentialsToken = "CLIENT-CREDENTIALS-TOKEN";
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans)).willReturn(clientCredentialsToken);
        var consentRequest = new ConsentRequest(LocalDate.ofInstant(clock.instant(), clock.getZone()).plusDays(90), 4);
        var redirectUrlWithState = redirectUrl + "?state=" + state;
        var consentId = "THE-CONSENT-ID";
        var banksScaRedirect = "https://receivedFromBank.com/redirectUserToIt";
        var consentResponse = mock(CreateConsentResponse.class);
        given(consentResponse.getConsentId()).willReturn(consentId);
        given(consentResponse.getScaRedirect()).willReturn(banksScaRedirect);
        given(httpClient.createUserConsent(clientCredentialsToken, consentRequest, redirectUrlWithState, psuIpAddress))
                .willReturn(consentResponse);
        var providerState = new RaiffeisenAtGroupProviderState(consentId);
        var serializedProviderState = "serializedProviderState";
        given(providerStateMapper.serialize(providerState)).willReturn(serializedProviderState);
        var expectedRedirectStep = new RedirectStep(banksScaRedirect, consentId, serializedProviderState);

        //when
        var result = authorizationService.getLoginInfo(httpClient, authenticationMeans, redirectUrl, psuIpAddress, state);

        //then
        assertThat(result).isEqualTo(expectedRedirectStep);
    }

    @Test
    void shouldThrowGetLoginInfoFailedExceptionWhenTokenInvalidExceptionIsThrownByHttpClient() throws TokenInvalidException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var redirectUrl = "https://baseredirecturl.com";
        var psuIpAddress = "127.0.0.1";
        var state = UUID.randomUUID().toString();
        var tokenInvalidException = new TokenInvalidException();
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans)).willThrow(tokenInvalidException);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.getLoginInfo(httpClient, authenticationMeans, redirectUrl, psuIpAddress, state);

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(call)
                .withMessage("Failed to get login info")
                .withCause(tokenInvalidException);
    }

    @Test
    void shouldThrowGetLoginInfoFailedExceptionWhenProviderStateProcessingExceptionIsThrownByProviderStateMapper() throws TokenInvalidException, ProviderStateProcessingException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var redirectUrl = "https://baseredirecturl.com";
        var psuIpAddress = "127.0.0.1";
        var state = UUID.randomUUID().toString();
        var clientCredentialsToken = "CLIENT-CREDENTIALS-TOKEN";
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans)).willReturn(clientCredentialsToken);
        var consentRequest = new ConsentRequest(LocalDate.ofInstant(clock.instant(), clock.getZone()).plusDays(90), 4);
        var redirectUrlWithState = redirectUrl + "?state=" + state;
        var consentId = "THE-CONSENT-ID";
        var consentResponse = mock(CreateConsentResponse.class);
        given(consentResponse.getConsentId()).willReturn(consentId);
        given(httpClient.createUserConsent(clientCredentialsToken, consentRequest, redirectUrlWithState, psuIpAddress))
                .willReturn(consentResponse);
        var providerState = new RaiffeisenAtGroupProviderState(consentId);
        var providerStateProcessingException = new ProviderStateProcessingException("Exception during mapping", new IOException());
        given(providerStateMapper.serialize(providerState)).willThrow(providerStateProcessingException);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.getLoginInfo(httpClient, authenticationMeans, redirectUrl, psuIpAddress, state);

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(call)
                .withMessage("Failed to get login info")
                .withCause(providerStateProcessingException);
    }

    @Test
    void shouldReturnCreatedAccessMeans() throws TokenInvalidException, ProviderStateProcessingException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var redirectUrlPostedBackFromSite = "https://yolt.com/callback?state=123";
        var userId = UUID.randomUUID();
        var psuIpAddress = "127.0.0.1";
        var serializedProviderState = "serializedProviderState";
        var clientToken = "THE-CLIENT-TOKEN";
        var consentId = "THE-CONSENT-ID";
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans)).willReturn(clientToken);
        var deserializedProviderState = new RaiffeisenAtGroupProviderState(consentId);
        given(providerStateMapper.deserialize(serializedProviderState)).willReturn(deserializedProviderState);
        var getConsentResponse = mock(GetConsentResponse.class);
        var expectedValidityDate = LocalDate.now().plusDays(10);
        given(getConsentResponse.getConsentStatus()).willReturn(ConsentStatus.VALID);
        given(getConsentResponse.getValidUntil()).willReturn(expectedValidityDate);
        given(httpClient.getConsentStatus(clientToken, consentId, psuIpAddress)).willReturn(getConsentResponse);
        var expectedAccessMeans = new AccessMeansDTO(
                userId,
                serializedProviderState,
                dateMapper.toDate(LocalDate.now()),
                dateMapper.toDate(expectedValidityDate));

        //when
        var result = authorizationService.createNewAccessMeans(httpClient, authenticationMeans, redirectUrlPostedBackFromSite, userId, psuIpAddress, serializedProviderState);

        //then
        assertThat(result.getAccessMeans()).usingRecursiveComparison().ignoringFields("created").isEqualTo(expectedAccessMeans);
        assertThat(result.getStep()).isNull();
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenRedirectUrlContainsError() {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var redirectUrlPostedBackFromSite = "https://yolt.com/callback?error=no-consent-from-user";
        var userId = UUID.randomUUID();
        var psuIpAddress = "127.0.0.1";
        var serializedProviderState = "serializedProviderState";

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.createNewAccessMeans(httpClient, authenticationMeans, redirectUrlPostedBackFromSite, userId, psuIpAddress, serializedProviderState);

        //then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(call)
                .withMessage("Redirect url contains error");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenConsentStatusIsDifferentThanValid() throws TokenInvalidException, ProviderStateProcessingException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var redirectUrlPostedBackFromSite = "https://yolt.com/callback?state=123";
        var userId = UUID.randomUUID();
        var psuIpAddress = "127.0.0.1";
        var serializedProviderState = "serializedProviderState";
        var clientToken = "THE-CLIENT-TOKEN";
        var consentId = "THE-CONSENT-ID";
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans)).willReturn(clientToken);
        var deserializedProviderState = new RaiffeisenAtGroupProviderState(consentId);
        given(providerStateMapper.deserialize(serializedProviderState)).willReturn(deserializedProviderState);
        var getConsentResponse = mock(GetConsentResponse.class);
        given(getConsentResponse.getConsentStatus()).willReturn(ConsentStatus.REJECTED);
        given(httpClient.getConsentStatus(clientToken, consentId, psuIpAddress)).willReturn(getConsentResponse);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.createNewAccessMeans(httpClient, authenticationMeans, redirectUrlPostedBackFromSite, userId, psuIpAddress, serializedProviderState);

        //then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(call)
                .withMessage("Consent isn't valid and can't be used");
    }

    @Test
    void shouldReturnGetAccessTokenFailedExceptionWhenProviderStateReturnExceptionDuringDeserialization() throws TokenInvalidException, ProviderStateProcessingException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var redirectUrlPostedBackFromSite = "https://yolt.com/callback?state=123";
        var userId = UUID.randomUUID();
        var psuIpAddress = "127.0.0.1";
        var serializedProviderState = "serializedProviderState";
        var clientToken = "THE-CLIENT-TOKEN";
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans)).willReturn(clientToken);
        given(providerStateMapper.deserialize(serializedProviderState)).willThrow(ProviderStateProcessingException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.createNewAccessMeans(httpClient, authenticationMeans, redirectUrlPostedBackFromSite, userId, psuIpAddress, serializedProviderState);

        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(call);
    }

    @Test
    void shouldReturnGetAccessTokenFailedExceptionWhenTokenInvalidExceptionIsThrownByHttpClient() throws TokenInvalidException, ProviderStateProcessingException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var redirectUrlPostedBackFromSite = "https://yolt.com/callback?state=123";
        var userId = UUID.randomUUID();
        var psuIpAddress = "127.0.0.1";
        var serializedProviderState = "serializedProviderState";
        var clientToken = "THE-CLIENT-TOKEN";
        var consentId = "THE-CONSENT-ID";
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans)).willReturn(clientToken);
        var deserializedProviderState = new RaiffeisenAtGroupProviderState(consentId);
        given(providerStateMapper.deserialize(serializedProviderState)).willReturn(deserializedProviderState);
        given(httpClient.getConsentStatus(clientToken, consentId, psuIpAddress)).willThrow(TokenInvalidException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.createNewAccessMeans(httpClient, authenticationMeans, redirectUrlPostedBackFromSite, userId, psuIpAddress, serializedProviderState);

        //then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(call);
    }

    @Test
    void shouldDeleteUserConsent() throws TokenInvalidException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var clientToken = "THE-CLIENT-TOKEN";
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans))
                .willReturn(clientToken);
        doNothing().when(httpClient).deleteUserConsent(clientToken, consentId, psuIpAddress);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.deleteUserConsent(httpClient, authenticationMeans, psuIpAddress, consentId);

        //then
        assertThatCode(call)
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenTOkenInvalidExceptionIsThrownByHttpClient() throws TokenInvalidException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var psuIpAddress = "127.0.0.1";
        var consentId = UUID.randomUUID().toString();
        var clientToken = "THE-CLIENT-TOKEN";
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans))
                .willReturn(clientToken);
        doThrow(TokenInvalidException.class)
                .when(httpClient).deleteUserConsent(clientToken, consentId, psuIpAddress);

        //when
        ThrowableAssert.ThrowingCallable call = () -> authorizationService.deleteUserConsent(httpClient, authenticationMeans, psuIpAddress, consentId);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call);
    }
}