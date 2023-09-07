package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.oauth2;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope.ACCOUNTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LloydsBasicOauthTokenBodyProducerDecoratorTest {

    private static final String FAKE_AUTH_CODE = "authCode";
    private static final String FAKE_CLIENT_ID = "someClientId";
    private static final String FAKE_REFRESH_TOKEN = "refreshToken";
    private static final String FAKE_REDIRECT_URL = "http://localhost/redirect";

    @Mock
    DefaultAuthMeans defaultAuthMeans;

    LloydsBasicOauthTokenBodyProducerDecorator sut;

    @BeforeEach
    void setUp() {
        when(defaultAuthMeans.getClientId()).thenReturn(FAKE_CLIENT_ID);
        sut = new LloydsBasicOauthTokenBodyProducerDecorator();
    }

    @Test
    void shouldReturnBodyForClientCredentialsGrant() {
        //given
        MultiValueMap<String, String> expectedBodyItems = new LinkedMultiValueMap<>();
        expectedBodyItems.add(OAuth.CLIENT_ID, FAKE_CLIENT_ID);
        expectedBodyItems.add(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);
        expectedBodyItems.add(OAuth.SCOPE, ACCOUNTS.getAuthorizationUrlScope());
        TokenScope tokenScope = TokenScope.builder()
                .authorizationUrlScope(ACCOUNTS.getAuthorizationUrlScope())
                .build();

        //when
        MultiValueMap<String, String> returnedBody = sut.getCreateClientCredentialsBody(defaultAuthMeans, tokenScope);

        //then
        assertThat(returnedBody).containsExactlyInAnyOrderEntriesOf(expectedBodyItems);
    }

    @Test
    void shouldReturnBodyForRefreshToken() {
        //given
        MultiValueMap<String, String> expectedBodyItems = new LinkedMultiValueMap<>();
        expectedBodyItems.add(OAuth.CLIENT_ID, FAKE_CLIENT_ID);
        expectedBodyItems.add(OAuth.GRANT_TYPE, OAuth.REFRESH_TOKEN);
        expectedBodyItems.add(OAuth.REFRESH_TOKEN, FAKE_REFRESH_TOKEN);

        //when
        MultiValueMap<String, String> returnedBody = sut.getRefreshAccessTokenBody(defaultAuthMeans, FAKE_REFRESH_TOKEN);

        //then
        assertThat(returnedBody).containsExactlyInAnyOrderEntriesOf(expectedBodyItems);
    }

    @Test
    void shouldReturnBodyForCreateToken() {
        //given
        MultiValueMap<String, String> expectedBodyItems = new LinkedMultiValueMap<>();
        expectedBodyItems.add(OAuth.CLIENT_ID, FAKE_CLIENT_ID);
        expectedBodyItems.add(OAuth.GRANT_TYPE, OAuth.AUTHORIZATION_CODE);
        expectedBodyItems.add(OAuth.REDIRECT_URI, FAKE_REDIRECT_URL);
        expectedBodyItems.add(OAuth.CODE, FAKE_AUTH_CODE);

        //when
        MultiValueMap<String, String> returnedBody = sut.getCreateAccessTokenBody(defaultAuthMeans, FAKE_AUTH_CODE, FAKE_REDIRECT_URL);

        //then
        assertThat(returnedBody).containsExactlyInAnyOrderEntriesOf(expectedBodyItems);
    }
}
