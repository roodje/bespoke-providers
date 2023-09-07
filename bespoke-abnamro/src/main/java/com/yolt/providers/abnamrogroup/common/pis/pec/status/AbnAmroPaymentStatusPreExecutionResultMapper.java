package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPisAccessTokenProvider;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateDeserializer;
import com.yolt.providers.abnamrogroup.common.pis.pec.exception.AbnAmroUserAccessTokenNotProvidedException;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Clock;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
public class AbnAmroPaymentStatusPreExecutionResultMapper implements SepaStatusPaymentPreExecutionResultMapper<AbnAmroPaymentStatusPreExecutionResult> {

    private static final int ACCESS_TOKEN_EXPIRATION_TIME_THRESHOLD_MINUTES = 1;

    private final AbnAmroPisAccessTokenProvider pisAccessTokenProvider;
    private final AbnAmroProviderStateDeserializer providerStateDeserializer;
    private final Clock clock;

    @Override
    public AbnAmroPaymentStatusPreExecutionResult map(GetStatusRequest getStatusRequest) {
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(getStatusRequest.getAuthenticationMeans());
        RestTemplateManager restTemplateManager = getStatusRequest.getRestTemplateManager();
        AbnAmroPaymentProviderState providerState = providerStateDeserializer.deserialize(getStatusRequest.getProviderState());
        AbnAmroPaymentProviderState.UserAccessTokenState userAccessTokenState = providerState.getUserAccessTokenState();
        if (userAccessTokenState != null) {
            userAccessTokenState = refreshUserAccessTokenIfNeeded(userAccessTokenState, restTemplateManager, authenticationMeans);
            providerState.setUserAccessTokenState(userAccessTokenState);
        } else {
            throw new AbnAmroUserAccessTokenNotProvidedException("Cannot check payment status due to lack of user access token");
        }

        return new AbnAmroPaymentStatusPreExecutionResult(providerState,
                authenticationMeans,
                restTemplateManager);
    }

    private AbnAmroPaymentProviderState.UserAccessTokenState refreshUserAccessTokenIfNeeded(AbnAmroPaymentProviderState.UserAccessTokenState userAccessTokenState,
                                                                                            RestTemplateManager restTemplateManager,
                                                                                            AbnAmroAuthenticationMeans authenticationMeans) {
        if (isExpirationTimePassed(userAccessTokenState.getExpirationZonedDateTime())) {
            try {
                AccessTokenResponseDTO accessTokenResponseDTO = pisAccessTokenProvider.provideAccessToken(restTemplateManager,
                        authenticationMeans,
                        prepareRefreshTokenRequestBody(authenticationMeans.getClientId(),
                                userAccessTokenState.getRefreshToken()));
                return new AbnAmroPaymentProviderState.UserAccessTokenState(accessTokenResponseDTO.getAccessToken(),
                        accessTokenResponseDTO.getRefreshToken(),
                        accessTokenResponseDTO.getExpiresIn(),
                        clock);
            } catch (TokenInvalidException e) {
                throw new IllegalStateException("Cannot refresh access token", e);
            }
        } else {
            return userAccessTokenState;
        }
    }

    private boolean isExpirationTimePassed(ZonedDateTime expirationDateTime) {
        return expirationDateTime == null || !expirationDateTime.minusMinutes(ACCESS_TOKEN_EXPIRATION_TIME_THRESHOLD_MINUTES)
                .isAfter(ZonedDateTime.now(clock));
    }

    private MultiValueMap<String, String> prepareRefreshTokenRequestBody(String clientId, String refreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(OAuth.GRANT_TYPE, OAuth.REFRESH_TOKEN);
        body.add(OAuth.CLIENT_ID, clientId);
        body.add(OAuth.REFRESH_TOKEN, refreshToken);
        return body;
    }
}
