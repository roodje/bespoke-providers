package com.yolt.providers.openbanking.ais.revolutgroup.common.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;

import java.util.List;
import java.util.function.Function;

public class RevolutMutualTlsOauth2ClientV1<T> extends BasicOauthClient {

    private static final List<String> CLIENT_IDS_FOR_NON_LICENSED_CLIENTS_AND_APP_PRD = List.of(
            "4616835c-f9d1-4db6-bb50-95519cc266cb", // RealTechnologies and YTS Credit Scoring App
            "219f5699-9aa0-4d1b-9494-da09362cbcfd"  // Revolut Eu on yfb-ext-prd for non-licensed clients (DNB)
    );

    private final Function<DefaultAuthMeans, String> authenticationHeaderSupplier;
    private final TokenRequestBodyProducer<T> tokenRequestBodySupplier;

    public RevolutMutualTlsOauth2ClientV1(String oAuthTokenUrl,
                                          Function<DefaultAuthMeans, String> authenticationHeaderSupplier,
                                          TokenRequestBodyProducer<T> tokenRequestBodySupplier,
                                          boolean isInPisProvider) {
        super(oAuthTokenUrl, authenticationHeaderSupplier, new BasicOauthTokenBodyProducer(), isInPisProvider);
        this.authenticationHeaderSupplier = authenticationHeaderSupplier;
        this.tokenRequestBodySupplier = tokenRequestBodySupplier;
    }

    @Override
    public AccessTokenResponseDTO createClientCredentials(final HttpClient httpClient,
                                                          final DefaultAuthMeans authenticationMeans,
                                                          final TokenScope scope,
                                                          final Signer signer) throws TokenInvalidException {
        if (CLIENT_IDS_FOR_NON_LICENSED_CLIENTS_AND_APP_PRD.contains(authenticationMeans.getClientId())) {
            T body = tokenRequestBodySupplier.getCreateClientCredentialsBody(authenticationMeans, scope);
            return createToken(httpClient, authenticationMeans, body, ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                    authenticationHeaderSupplier);
        }

        return super.createClientCredentials(httpClient, authenticationMeans, scope, signer);
    }
}
