package com.yolt.providers.starlingbank.common.service.fetchdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.mapper.StarlingBankTokenMapper;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StarlingBankStarlingBankFetchDataServiceV6Test {

    private static final String GET_ACCOUNTS_URL = "/api/v2/accounts";
    private static final UUID USER_ID = UUID.fromString("2c38d93e-3343-4a54-bf4f-756c0c99982e");
    private static final String ACCESS_TOKEN_INVALID = "access-token-invalid";

    private StarlingBankTokenMapper tokenMapper;
    private StarlingBankFetchDataServiceV6 fetchDataService;
    private StarlingBankHttpClient httpClientMock;
    private Clock clock;

    @BeforeEach
    void beforeEach() {
        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(Instant.now());
        tokenMapper = new StarlingBankTokenMapper(new ObjectMapper(), clock);
        fetchDataService = new StarlingBankFetchDataServiceV6(tokenMapper, clock);
        httpClientMock = Mockito.mock(StarlingBankHttpClient.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionForGetAccountsAndTransactionsWhenInvalidToken() throws TokenInvalidException {
        // given
        when(httpClientMock.fetchAccounts(GET_ACCOUNTS_URL, ACCESS_TOKEN_INVALID)).thenThrow(TokenInvalidException.class);

        // when
        ThrowableAssert.ThrowingCallable getAccountsAndTransactionsCallable = () ->
                fetchDataService.getAccountsAndTransactions(httpClientMock, createAccessMeans(), Instant.now(clock));

        // then
        assertThatThrownBy(getAccountsAndTransactionsCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    private AccessMeansDTO createAccessMeans() {
        Token oAuthToken = new Token();
        oAuthToken.setUserId(USER_ID.toString());
        oAuthToken.setAccessToken(ACCESS_TOKEN_INVALID);
        oAuthToken.setRefreshToken("refresh-token");
        oAuthToken.setExpiresIn(300);
        return tokenMapper.mapToAccessMeansDTO(USER_ID, oAuthToken);
    }
}
