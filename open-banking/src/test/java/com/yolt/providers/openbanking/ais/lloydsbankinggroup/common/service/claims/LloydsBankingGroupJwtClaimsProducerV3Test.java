package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.claims;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.FapiCompliantJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.assertj.core.data.Offset;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LloydsBankingGroupJwtClaimsProducerV3Test {

    private final LloydsBankingGroupJwtClaimsProducerV3 sut = new LloydsBankingGroupJwtClaimsProducerV3(
            new FapiCompliantJwtClaimsProducerDecorator(
                    new DefaultJwtClaimsProducer(
                            DefaultAuthMeans::getClientId,
                            "lbg"
                    )), new LLoydsBankingGroupNonceProvider());

    @Test
    public void shouldReturnFAPICompliantJwtClaimsForCreateJwtClaimWithCorrectData() throws MalformedClaimException {
        // given
        DefaultAuthMeans defaultAuthMeans = mock(DefaultAuthMeans.class);
        when(defaultAuthMeans.getClientId())
                .thenReturn("client123");
        String secretState = "1234567890";
        String redirectUrl = "http://www.yolt.com/redirect";
        TokenScope tokenScope = TokenScope.builder()
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
        NumericDate expirationTime = NumericDate.now();
        expirationTime.addSeconds(3600);

        // when
        JwtClaims result = sut.createJwtClaims(defaultAuthMeans, secretState, redirectUrl, tokenScope);

        // then
        assertThat(result.getIssuer()).isEqualTo("client123");
        assertThat(result.getAudience()).contains("lbg");
        assertThat(result.getClaimValue(OAuth.RESPONSE_TYPE)).isEqualTo("code id_token");
        assertThat(result.getClaimValue(OAuth.CLIENT_ID)).isEqualTo("client123");
        assertThat(result.getClaimValue(OAuth.REDIRECT_URI)).isEqualTo("http://www.yolt.com/redirect");
        assertThat(result.getClaimValue(OAuth.SCOPE)).isEqualTo(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope());
        assertThat(result.getClaimValue(OAuth.STATE)).isEqualTo("1234567890");
        assertThat(result.getClaimValue(OAuth.NONCE)).isEqualTo("12345678");
        assertThat(result.getClaimValue(OAuth.MAX_AGE)).isEqualTo(TimeUnit.DAYS.toSeconds(1));
        assertThat(result.getExpirationTime().getValueInMillis()).isCloseTo(expirationTime.getValueInMillis(), Offset.offset(5L));
    }
}
