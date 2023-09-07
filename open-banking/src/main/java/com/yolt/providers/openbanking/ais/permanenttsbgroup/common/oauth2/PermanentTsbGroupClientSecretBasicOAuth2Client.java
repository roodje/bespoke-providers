package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.common.HttpUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.ClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.security.cert.CertificateEncodingException;
import java.util.function.Function;

public class PermanentTsbGroupClientSecretBasicOAuth2Client extends BasicOauthClient<MultiValueMap<String, String>> implements PermanentTsbGroupOauth2Client {

    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER_NAME = "tpp-signature-certificate";

    private final TokenRequestBodyProducer<MultiValueMap<String, String>> tokenRequestBodyProducer;
    private final ClientAssertionProducer clientAssertionProducer;
    private final Function<DefaultAuthMeans, String> createCredentialsAuthenticationHeaderSupplier;
    private final PermanentTsbGroupTppSignatureCertificateHeaderProducer tppSignatureCertificateHeaderProducer;
    private final String oAuthTokenUrl;

    public PermanentTsbGroupClientSecretBasicOAuth2Client(PermanentTsbGroupTokenBodyProducer tokenBodyProducer,
                                                          ClientAssertionProducer clientAssertionProducer,
                                                          PermanentTsbGroupTppSignatureCertificateHeaderProducer tsbGroupTppSignatureCertificateHeaderProducer,
                                                          DefaultProperties properties,
                                                          boolean isInPisFlow) {
        super(properties.getOAuthTokenUrl(),
                authenticationMeans -> HttpUtils.basicCredentials(authenticationMeans.getClientId(), authenticationMeans.getClientSecret()),
                tokenBodyProducer,
                isInPisFlow);
        this.oAuthTokenUrl = properties.getOAuthTokenUrl();
        this.tokenRequestBodyProducer = tokenBodyProducer;
        this.clientAssertionProducer = clientAssertionProducer;
        this.tppSignatureCertificateHeaderProducer = tsbGroupTppSignatureCertificateHeaderProducer;
        this.createCredentialsAuthenticationHeaderSupplier = authenticationMeans -> HttpUtils.basicCredentials(authenticationMeans.getClientId(), authenticationMeans.getClientSecret());
    }

    @Override
    public AccessTokenResponseDTO createClientCredentials(final HttpClient httpClient,
                                                          final DefaultAuthMeans authenticationMeans,
                                                          final TokenScope scope,
                                                          final Signer signer) throws TokenInvalidException {
        MultiValueMap<String, String> body = tokenRequestBodyProducer.getCreateClientCredentialsBody(authenticationMeans, scope,
                clientAssertionProducer.createNewClientRequestToken(authenticationMeans, signer));
        return createToken(httpClient, authenticationMeans, body, ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                createCredentialsAuthenticationHeaderSupplier);
    }

    @Override
    protected AccessTokenResponseDTO createToken(final HttpClient httpClient,
                                                 final DefaultAuthMeans authenticationMeans,
                                                 final MultiValueMap<String, String> body,
                                                 final String endpointIdentifier,
                                                 Function<DefaultAuthMeans, String> authenticationHeaderSupplier) throws TokenInvalidException {
        HttpHeaders headers = getHeaders(authenticationHeaderSupplier.apply(authenticationMeans), authenticationMeans);
        return httpClient.exchange(oAuthTokenUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                endpointIdentifier,
                AccessTokenResponseDTO.class,
                getErrorHandler()).getBody();
    }

    @Override
    public String getAuthorizationUrl(final HttpClient httpClient,
                                      final String authorizationUrl) throws TokenInvalidException {
        return httpClient.exchange(authorizationUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                ProviderClientEndpoints.GET_AUTHORIZE_CONSENT,
                String.class).getBody();
    }

    protected HttpHeaders getHeaders(final String authenticationHeader, final DefaultAuthMeans authenticationMeans) {
        HttpHeaders headers = super.getHeaders(authenticationHeader, authenticationMeans.getInstitutionId());
        try {
            headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER_NAME, tppSignatureCertificateHeaderProducer.getTppSignatureCertificateHeaderValue(authenticationMeans.getSigningCertificate()));
        } catch (CertificateEncodingException e) {
            throw new GetAccessTokenFailedException("Could not encode TPP signature certificate", e);
        }

        return headers;
    }
}
