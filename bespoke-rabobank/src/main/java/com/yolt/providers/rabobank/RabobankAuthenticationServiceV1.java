package com.yolt.providers.rabobank;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.rabobank.config.RabobankProperties;
import com.yolt.providers.rabobank.dto.AccessTokenResponseDTO;
import com.yolt.providers.rabobank.http.RabobankAisHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class RabobankAuthenticationServiceV1 implements RabobankAuthenticationService {

    private final RabobankProperties properties;
    private final Clock clock;

    private static final DateTimeFormatter RABOBANK_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O", Locale.ENGLISH);
    private static final List<String> AIS_HEADERS_TO_SIGN = Arrays.asList("digest", "date", "x-request-id");

    @Override
    public String generateAuthorizationUrl(String clientId,
                                           String redirectUrl,
                                           String state) {
        MultiValueMap<String, String> varMap = new LinkedMultiValueMap<>();
        varMap.add("response_type", "code");
        varMap.add("client_id", clientId);
        varMap.add("scope", properties.getOAuthAuthorizationScope());
        varMap.add("redirect_uri", redirectUrl);
        varMap.add("state", state);
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseAuthorizationUrl() + "oauth2/authorize")
                .queryParams(varMap)
                .build()
                .encode()
                .toString();
    }

    @Override
    public AccessTokenResponseDTO getAccessToken(RabobankAuthenticationMeans authenticationMeans,
                                                 RestTemplate restTemplate,
                                                 String redirectUrlPostedBackFromSite) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(authenticationMeans.getClientId(), authenticationMeans.getClientSecret());
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap().get("code"));
        try {
            return restTemplate.exchange(properties.getBaseAuthorizationUrl() + "oauth2/token",
                    HttpMethod.POST,
                    new HttpEntity<>(body, httpHeaders),
                    AccessTokenResponseDTO.class).getBody();
        } catch (RestClientException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    @Override
    public AccessTokenResponseDTO refreshToken(RabobankAuthenticationMeans authenticationMeans,
                                               RabobankAisHttpClient httpClient,
                                               AccessTokenResponseDTO oldToken) throws TokenInvalidException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(authenticationMeans.getClientId(), authenticationMeans.getClientSecret());
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> refreshTokenRequest = new LinkedMultiValueMap<>();
        refreshTokenRequest.add("grant_type", "refresh_token");
        refreshTokenRequest.add("refresh_token", oldToken.getRefreshToken());
        AccessTokenResponseDTO token = httpClient.refreshAccessMeans(new HttpEntity<>(refreshTokenRequest, httpHeaders));
        token.setMetadata(oldToken.getMetadata());
        return token;
    }

    @SneakyThrows(CertificateEncodingException.class)
    @Override
    public GetConsentResponse getConsent(RabobankAuthenticationMeans authenticationMeans,
                                         RestTemplate restTemplate,
                                         Signer signer,
                                         String consentId) {
        X509Certificate clientSigningCertificate = authenticationMeans.getClientSigningCertificate();
        String encodedSigningCertificate = Base64.toBase64String(clientSigningCertificate.getEncoded());

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-request-id", ExternalTracingUtil.createLastExternalTraceId());
        headers.add("x-ibm-client-id", authenticationMeans.getClientId());
        headers.add("tpp-signature-certificate", encodedSigningCertificate);
        headers.add("date", RABOBANK_DATETIME_FORMATTER.format(ZonedDateTime.now(clock)));
        headers.add("digest", SigningUtil.getDigest(new byte[]{}));
        headers.add("signature", SigningUtil.getSigningString(signer,
                headers,
                clientSigningCertificate.getSerialNumber().toString(),
                authenticationMeans.getSigningKid(),
                AIS_HEADERS_TO_SIGN));

        return restTemplate.exchange(String.format("/oauth2/v1/consents/%s", consentId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GetConsentResponse.class).getBody();
    }
}
