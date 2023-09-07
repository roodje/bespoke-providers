package com.yolt.providers.bunq.common.service.autoonboarding;

import com.bunq.sdk.security.SecurityUtils;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.exception.BunqPsd2RegisterProviderException;
import com.yolt.providers.bunq.common.http.BunqHttpServiceV5;
import com.yolt.providers.bunq.common.model.*;
import com.yolt.providers.bunq.common.util.BunqCertificateFormatter;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.security.KeyPair;
import java.util.*;
import java.util.stream.Collectors;

import static com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2.*;

@RequiredArgsConstructor
public class BunqAutoOnboardingServiceV2 {


    private final BunqCertificateFormatter certificateFormatter;

    public Map<String, TypedAuthenticationMeans> getAutoConfigureMeans() {
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = new HashMap<>();
        autoConfiguredMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        autoConfiguredMeans.put(CLIENT_SECRET_STRING, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        autoConfiguredMeans.put(PSD2_USER_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        autoConfiguredMeans.put(OAUTH_USER_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        autoConfiguredMeans.put(PSD2_API_KEY, TypedAuthenticationMeans.API_KEY_STRING);

        return autoConfiguredMeans;
    }

    public Map<String, BasicAuthenticationMean> registerProvider(UrlAutoOnboardingRequest urlAutoOnboardingRequest, BunqHttpServiceV5 httpService) {
        try {
            Map<String, BasicAuthenticationMean> mutableAuthenticationMeans = new HashMap<>(urlAutoOnboardingRequest.getAuthenticationMeans());
            KeyPair keyPair = SecurityUtils.generateKeyPair();
            InstallationResponse installation = httpService.createInstallation(keyPair);
            String installationToken = installation.getToken().getTokenString();
            Psd2ProviderResponse providerResponse = registerProviderCertificates(keyPair, installationToken, httpService, urlAutoOnboardingRequest);
            httpService.createDeviceServer(keyPair, installationToken, providerResponse.getCredentialPasswordIp().getTokenValue());
            Psd2SessionResponse session = httpService.createPsd2SessionServer(keyPair, installationToken, providerResponse.getCredentialPasswordIp().getTokenValue());
            OauthClientDetailsResponse oauthClientLists = httpService.getOAuthClientList(keyPair, session.getToken().getTokenString(), session.getPsd2UserId());
            if (!Objects.isNull(oauthClientLists.getOAuthClientId())) {
                return addCallbackUrlToExistingOauthClientIfNotExists(
                        mutableAuthenticationMeans,
                        keyPair,
                        providerResponse,
                        session,
                        oauthClientLists,
                        urlAutoOnboardingRequest.getBaseClientRedirectUrl(),
                        httpService);
            }
            OauthClientRegistrationResponse clientRegistrationResponse = httpService.registerOAuthClient(
                    keyPair,
                    session.getToken().getTokenString(),
                    session.getPsd2UserId());

            OauthClientDetailsResponse clientDetailResponse = httpService.getOAuthClientDetails(
                    keyPair,
                    session.getToken().getTokenString(),
                    session.getPsd2UserId(),
                    clientRegistrationResponse.getOAuthClientId()
            );
            httpService.addCalbackUrl(
                    keyPair,
                    session.getToken().getTokenString(),
                    session.getPsd2UserId(),
                    clientRegistrationResponse.getOAuthClientId(),
                    urlAutoOnboardingRequest.getBaseClientRedirectUrl()
            );
            updateClientIdAndClientSecret(mutableAuthenticationMeans, providerResponse, session, clientDetailResponse);
            return mutableAuthenticationMeans;
        } catch (Exception e) {
            throw new BunqPsd2RegisterProviderException("Error during register psd2 provider", e);
        }
    }

    private Map<String, BasicAuthenticationMean> addCallbackUrlToExistingOauthClientIfNotExists(Map<String, BasicAuthenticationMean> mutableAuthenticationMeans,
                                                                                                KeyPair keyPair,
                                                                                                Psd2ProviderResponse providerResponse,
                                                                                                Psd2SessionResponse session,
                                                                                                OauthClientDetailsResponse oauthClientLists,
                                                                                                String callbackUrl,
                                                                                                BunqHttpServiceV5 httpService) throws TokenInvalidException {
        if (oauthClientLists.getCallbackUrls().stream().noneMatch(url -> url.getUrl().equals(callbackUrl))) {
            httpService.addCalbackUrl(
                    keyPair,
                    session.getToken().getTokenString(),
                    session.getPsd2UserId(),
                    oauthClientLists.getOAuthClientId(),
                    callbackUrl
            );
        }
        updateClientIdAndClientSecret(mutableAuthenticationMeans, providerResponse, session, oauthClientLists);
        return mutableAuthenticationMeans;
    }

    private void updateClientIdAndClientSecret(Map<String, BasicAuthenticationMean> mutableAuthenticationMeans,
                                               Psd2ProviderResponse provider,
                                               Psd2SessionResponse session,
                                               OauthClientDetailsResponse oauthClientDetails) {
        mutableAuthenticationMeans.put(PSD2_API_KEY, new BasicAuthenticationMean(TypedAuthenticationMeans.API_KEY_STRING.getType(), provider.getCredentialPasswordIp().getTokenValue()));
        mutableAuthenticationMeans.put(PSD2_USER_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), String.valueOf(session.getPsd2UserId())));
        mutableAuthenticationMeans.put(OAUTH_USER_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), String.valueOf(oauthClientDetails.getOAuthClientId())));
        mutableAuthenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), oauthClientDetails.getClientId()));
        mutableAuthenticationMeans.put(CLIENT_SECRET_STRING, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SECRET_STRING.getType(), oauthClientDetails.getClientSecret()));
    }

    private Psd2ProviderResponse registerProviderCertificates(KeyPair keyPair, String installationToken, BunqHttpServiceV5 httpService, UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        String publicCertificate = certificateFormatter.formatCertificateString(urlAutoOnboardingRequest.getAuthenticationMeans().get(SIGNING_CERTIFICATE).getValue());
        String fullCertificateChain = urlAutoOnboardingRequest.getAuthenticationMeans().get(SIGNING_CERTIFICATE_CHAIN).getValue();
        String chainWithoutLeafCertificate = certificateFormatter.removeLeafCertificateFromChain(fullCertificateChain);
        String publicCertificateChain = certificateFormatter.removeNewLinesAndAddNewOnesInProperPlaceInCertificateOrChain(chainWithoutLeafCertificate);
        Signer signer = urlAutoOnboardingRequest.getSigner();
        String toSignString = SecurityUtils.getPublicKeyFormattedString(keyPair.getPublic()) + installationToken;
        String signedString = signer.sign(
                toSignString.getBytes(),
                UUID.fromString(urlAutoOnboardingRequest.getAuthenticationMeans().get(SIGNING_PRIVATE_KEY_ID).getValue()),
                SignatureAlgorithm.SHA256_WITH_RSA
        );
        Psd2ProviderRequest psd2ProviderRequest = new Psd2ProviderRequest(publicCertificate, publicCertificateChain, signedString);
        return httpService.registerProvider(keyPair, psd2ProviderRequest, installationToken);
    }

    public void removeCallbackFromProvider(UrlAutoOnboardingRequest urlAutoOnboardingRequest, String provider, BunqHttpServiceV5 httpService) {
        try {
            if (ObjectUtils.isEmpty(urlAutoOnboardingRequest.getAuthenticationMeans().get(CLIENT_ID))) {
                //not reqistred in bunq, nothing to do
                return;
            }
            BunqAuthenticationMeansV2 authenticationMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(urlAutoOnboardingRequest.getAuthenticationMeans(), provider);
            String baseClientRedirectUrl = urlAutoOnboardingRequest.getBaseClientRedirectUrl();
            KeyPair keyPair = SecurityUtils.generateKeyPair();
            InstallationResponse installation = httpService.createInstallation(keyPair);
            String installationToken = installation.getToken().getTokenString();
            httpService.createDeviceServer(keyPair, installationToken, authenticationMeans.getPsd2apiKey());
            Psd2SessionResponse session = httpService.createPsd2SessionServer(keyPair, installationToken, authenticationMeans.getPsd2apiKey());
            //get info about oauth client
            OauthClientDetailsResponse clientDetailResponse = httpService.getOAuthClientDetails(
                    keyPair,
                    session.getToken().getTokenString(),
                    authenticationMeans.getPsd2UserId(),
                    authenticationMeans.getOauthUserId()
            );
            List<CallbackUrl> callbackUrlToRemove = clientDetailResponse.getCallbackUrls().stream().filter(o -> o.getUrl().equals(baseClientRedirectUrl)).collect(Collectors.toList());
            for (CallbackUrl url : callbackUrlToRemove) {
                httpService.removeCallbackUrl(keyPair, session.getToken().getTokenString(), authenticationMeans.getPsd2UserId(), authenticationMeans.getOauthUserId(), url.getId());
            }
        } catch (TokenInvalidException e) {
            throw new BunqPsd2RegisterProviderException("Error during removing callback url from bunq registration", e);
        }
    }

}
