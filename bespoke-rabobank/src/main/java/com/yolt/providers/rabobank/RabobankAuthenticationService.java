package com.yolt.providers.rabobank;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.rabobank.dto.AccessTokenResponseDTO;
import com.yolt.providers.rabobank.http.RabobankAisHttpClient;
import org.springframework.web.client.RestTemplate;

public interface RabobankAuthenticationService {

    String generateAuthorizationUrl(String clientId,
                                    String redirectUrl,
                                    String state);

    AccessTokenResponseDTO getAccessToken(RabobankAuthenticationMeans authenticationMeans,
                                          RestTemplate restTemplate,
                                          String redirectUrlPostedBackFromSite);

    AccessTokenResponseDTO refreshToken(RabobankAuthenticationMeans authenticationMeans,
                                        RabobankAisHttpClient httpClient,
                                        AccessTokenResponseDTO oldToken) throws TokenInvalidException;

    GetConsentResponse getConsent(RabobankAuthenticationMeans authenticationMeans,
                                  RestTemplate restTemplate,
                                  Signer signer,
                                  String consentId);
}
