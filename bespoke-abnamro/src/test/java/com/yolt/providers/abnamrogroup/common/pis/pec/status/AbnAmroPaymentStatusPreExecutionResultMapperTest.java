package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPisAccessTokenProvider;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateDeserializer;
import com.yolt.providers.abnamrogroup.common.pis.pec.exception.AbnAmroUserAccessTokenNotProvidedException;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.GetStatusRequestBuilder;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroPaymentStatusPreExecutionResultMapperTest {

    private AbnAmroPaymentStatusPreExecutionResultMapper subject;

    @Mock
    private AbnAmroPisAccessTokenProvider pisAccessTokenProvider;

    @Mock
    private AbnAmroProviderStateDeserializer providerStateDeserializer;

    @Mock
    private RestTemplateManager restTemplateManager;

    private final Clock clock = Clock.fixed(ZonedDateTime.parse("2021-07-02T12:00:00+02:00[Europe/Amsterdam]").toInstant(), ZoneId.of("Europe/Amsterdam"));

    @Captor
    private ArgumentCaptor<MultiValueMap<String, String>> bodyArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        subject = new AbnAmroPaymentStatusPreExecutionResultMapper(pisAccessTokenProvider, providerStateDeserializer, clock);
    }

    @Test
    void shouldThrowAbnAmroUserAccessTokenNotProvidedExceptionWhenUserAccessTokenStateIsNotProvidedInProviderState() {
        // give
        Map<String, BasicAuthenticationMean> authMeans = new AbnAmroTestPisAuthenticationMeans().getAuthMeans();
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setAuthenticationMeans(authMeans)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("providerState")
                .build();
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState("transactionId", null, null);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(providerState);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> subject.map(getStatusRequest);

        // then
        assertThatExceptionOfType(AbnAmroUserAccessTokenNotProvidedException.class)
                .isThrownBy(throwingCallable)
                .withMessage("Cannot check payment status due to lack of user access token");
    }

    @Test
    void shouldReturnPreExecutionResultWithUserAccessTokenStateWithRefreshedUserAccessTokenWhenUserAccessTokenIsExpired() throws TokenInvalidException {
        // give
        Map<String, BasicAuthenticationMean> authMeans = new AbnAmroTestPisAuthenticationMeans().getAuthMeans();
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(authMeans);
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setAuthenticationMeans(authMeans)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("providerState")
                .build();
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState("transactionId",
                null,
                new AbnAmroPaymentProviderState.UserAccessTokenState("oldAccessToken",
                        "oldRefreshToken",
                        -1,
                        clock));

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(providerState);
        given(pisAccessTokenProvider.provideAccessToken(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class), any(MultiValueMap.class)))
                .willReturn(new AccessTokenResponseDTO("newAccessToken", "newRefreshToken", 7200, null, null));

        // when
        AbnAmroPaymentStatusPreExecutionResult result = subject.map(getStatusRequest);

        // then
        then(providerStateDeserializer)
                .should()
                .deserialize("providerState");
        then(pisAccessTokenProvider)
                .should()
                .provideAccessToken(eq(restTemplateManager), eq(authenticationMeans), bodyArgumentCaptor.capture());
        MultiValueMap<String, String> capturedBody = bodyArgumentCaptor.getValue();
        assertThat(capturedBody.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                OAuth.GRANT_TYPE, OAuth.REFRESH_TOKEN,
                OAuth.CLIENT_ID, "TPP_test",
                OAuth.REFRESH_TOKEN, "oldRefreshToken"
        ));
        assertThat(result.getProviderState()).satisfies(state -> {
            assertThat(state.getTransactionId()).isEqualTo("transactionId");
            assertThat(state.getUserAccessTokenState()).satisfies(userAccessTokenState -> {
                assertThat(userAccessTokenState.getAccessToken()).isEqualTo("newAccessToken");
                assertThat(userAccessTokenState.getRefreshToken()).isEqualTo("newRefreshToken");
                assertThat(userAccessTokenState.getExpirationZonedDateTime()).isEqualTo(ZonedDateTime.now(clock).plusHours(2));
            });
        });
        assertThat(result.getAuthenticationMeans()).isEqualTo(authenticationMeans);
        assertThat(result.getRestTemplateManager()).isEqualTo(restTemplateManager);
    }

    @Test
    void shouldReturnPreExecutionResultWithUserAccessTokenStateWithRefreshedUserAccessTokenWhenUserAccessTokenIsCloseToBeExpiredWithin60Seconds() throws TokenInvalidException {
        // give
        Map<String, BasicAuthenticationMean> authMeans = new AbnAmroTestPisAuthenticationMeans().getAuthMeans();
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(authMeans);
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setAuthenticationMeans(authMeans)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("providerState")
                .build();
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState("transactionId",
                null,
                new AbnAmroPaymentProviderState.UserAccessTokenState("oldAccessToken",
                        "oldRefreshToken",
                        60,
                        clock));

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(providerState);
        given(pisAccessTokenProvider.provideAccessToken(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class), any(MultiValueMap.class)))
                .willReturn(new AccessTokenResponseDTO("newAccessToken", "newRefreshToken", 7200, null, null));

        // when
        AbnAmroPaymentStatusPreExecutionResult result = subject.map(getStatusRequest);

        // then
        then(providerStateDeserializer)
                .should()
                .deserialize("providerState");
        then(pisAccessTokenProvider)
                .should()
                .provideAccessToken(eq(restTemplateManager), eq(authenticationMeans), bodyArgumentCaptor.capture());
        MultiValueMap<String, String> capturedBody = bodyArgumentCaptor.getValue();
        assertThat(capturedBody.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                OAuth.GRANT_TYPE, OAuth.REFRESH_TOKEN,
                OAuth.CLIENT_ID, "TPP_test",
                OAuth.REFRESH_TOKEN, "oldRefreshToken"
        ));
        assertThat(result.getProviderState()).satisfies(state -> {
            assertThat(state.getTransactionId()).isEqualTo("transactionId");
            assertThat(state.getUserAccessTokenState()).satisfies(userAccessTokenState -> {
                assertThat(userAccessTokenState.getAccessToken()).isEqualTo("newAccessToken");
                assertThat(userAccessTokenState.getRefreshToken()).isEqualTo("newRefreshToken");
                assertThat(userAccessTokenState.getExpirationZonedDateTime()).isEqualTo(ZonedDateTime.now(clock).plusHours(2));
            });
        });
        assertThat(result.getAuthenticationMeans()).isEqualTo(authenticationMeans);
        assertThat(result.getRestTemplateManager()).isEqualTo(restTemplateManager);
    }

    @Test
    void shouldReturnPreExecutionResultWithUserAccessTokenStateWithTheSameUserAccessTokenWhenUserAccessTokenIsNotExpired() {
        // give
        Map<String, BasicAuthenticationMean> authMeans = new AbnAmroTestPisAuthenticationMeans().getAuthMeans();
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(authMeans);
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setAuthenticationMeans(authMeans)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("providerState")
                .build();
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState("transactionId",
                null,
                new AbnAmroPaymentProviderState.UserAccessTokenState("oldAccessToken",
                        "oldRefreshToken",
                        3600,
                        clock));

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(providerState);

        // when
        AbnAmroPaymentStatusPreExecutionResult result = subject.map(getStatusRequest);

        // then
        then(providerStateDeserializer)
                .should()
                .deserialize("providerState");
        then(pisAccessTokenProvider)
                .shouldHaveNoInteractions();
        assertThat(result.getProviderState()).satisfies(state -> {
            assertThat(state.getTransactionId()).isEqualTo("transactionId");
            assertThat(state.getUserAccessTokenState()).satisfies(userAccessTokenState -> {
                assertThat(userAccessTokenState.getAccessToken()).isEqualTo("oldAccessToken");
                assertThat(userAccessTokenState.getRefreshToken()).isEqualTo("oldRefreshToken");
                assertThat(userAccessTokenState.getExpirationZonedDateTime()).isEqualTo(ZonedDateTime.now(clock).plusHours(1));
            });
        });
        assertThat(result.getAuthenticationMeans()).isEqualTo(authenticationMeans);
        assertThat(result.getRestTemplateManager()).isEqualTo(restTemplateManager);
    }
}