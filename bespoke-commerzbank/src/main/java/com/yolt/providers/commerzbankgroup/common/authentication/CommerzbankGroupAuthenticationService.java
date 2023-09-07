package com.yolt.providers.commerzbankgroup.common.authentication;

import com.yolt.providers.commerzbankgroup.common.api.CommerzbankGroupApiClient;
import com.yolt.providers.commerzbankgroup.common.api.dto.authorization.AccountAccessEnum;
import com.yolt.providers.commerzbankgroup.common.api.dto.authorization.Consents;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import static com.yolt.providers.commerzbankgroup.common.api.CommerzbankGroupApiClient.*;

public class CommerzbankGroupAuthenticationService {

    private static final long CONSENT_VALIDITY_IN_DAYS = 90;
    private final Clock clock;

    public CommerzbankGroupAuthenticationService(Clock clock) {
        this.clock = clock;
    }

    public ConsentData getLoginInfo(String baseRedirectUri, String state, String organizationIdentifier, Supplier<CommerzbankGroupApiClient> commerzbankGroupApiClientSupplier) {
        var commerzbankGroupApiClient = commerzbankGroupApiClientSupplier.get();
        var consents = new Consents().access(new AccountAccessEnum().allPsd2(AccountAccessEnum.AllPsd2Enum.ALLACCOUNTS))
                .recurringIndicator(Boolean.TRUE)
                .frequencyPerDay(4)
                .combinedServiceIndicator(Boolean.FALSE)
                .validUntil(LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        var consentResponse = commerzbankGroupApiClient.createConsent(consents, baseRedirectUri);
        var oAuthLinksResponse = commerzbankGroupApiClient.fetchAuthorizationServerUrl(consentResponse.getLinks().getScaOAuth().getHref());
        var consentId = consentResponse.getConsentId();
        var randomS256 = OAuth2ProofKeyCodeExchange.createRandomS256();
        var codeVerifier = randomS256.getCodeVerifier();

        // The below is tricky: I assume whatever comes in authorization endpoint is already url encoded, so I shouldn't
        // encode the whole thing again
        var consentUrl = UriComponentsBuilder.fromHttpUrl(oAuthLinksResponse.getAuthorizationEndpoint())
                .queryParam("client_id", organizationIdentifier)
                .queryParam("scope", UriUtils.encode("AIS:" + consentId, StandardCharsets.UTF_8))
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("code_challenge", randomS256.getCodeChallenge())
                .queryParam("code_challenge_method", "S256")
                .queryParam("redirect_uri", UriUtils.encode(baseRedirectUri, StandardCharsets.UTF_8))
                .build()
                .toString();
        return new ConsentData(consentUrl, consentId, codeVerifier);
    }


    public AccessAndRefreshToken createNewAccessMeans(String redirectUrlPostedBackFromSite,
                                                      String baseClientRedirectUrl,
                                                      String state,
                                                      CommerzbankGroupProviderState commerzbankGroupProviderState,
                                                      Supplier<CommerzbankGroupApiClient> commerzbankGroupApiClientSupplier) {
        var commerzbankGroupApiClient = commerzbankGroupApiClientSupplier.get();
        var authorizationCode = UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap().get("code");
        if (StringUtils.isBlank(authorizationCode)) {
            throw new MissingDataException("Missing data for key code");
        }
        var codeVerifier = commerzbankGroupProviderState.codeVerifier();
        var commerzbankGroupTokenResponse = commerzbankGroupApiClient.fetchAccessToken(new CreateAccessTokenRequest(state, authorizationCode, codeVerifier, baseClientRedirectUrl));
        return new AccessAndRefreshToken(commerzbankGroupTokenResponse.getAccessToken(), commerzbankGroupTokenResponse.getRefreshToken(), commerzbankGroupTokenResponse.getExpiresIn());
    }

    public AccessAndRefreshToken refreshAccessMeans(String refreshToken, Supplier<CommerzbankGroupApiClient> commerzbankGroupApiClientSupplier) throws TokenInvalidException {
        var commerzbankGroupApiClient = commerzbankGroupApiClientSupplier.get();
        var commerzbankGroupTokenResponse = commerzbankGroupApiClient.refreshAccessToken(new RefreshAccessTokenRequest(refreshToken));
        return new AccessAndRefreshToken(commerzbankGroupTokenResponse.getAccessToken(), refreshToken, commerzbankGroupTokenResponse.getExpiresIn());
    }
}
