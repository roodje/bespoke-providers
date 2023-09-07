package com.yolt.providers.amexgroup.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeansV6;
import com.yolt.providers.amexgroup.common.dto.RevokeTokenResponse;
import com.yolt.providers.amexgroup.common.dto.TokenResponse;
import com.yolt.providers.amexgroup.common.dto.TokenResponses;
import com.yolt.providers.amexgroup.common.utils.AmexMacHeaderUtilsV2;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.amexgroup.common.utils.AmexAuthMeansFields.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AmexGroupAuthenticationServiceV3Test {

    @Mock
    private AmexGroupRestTemplateServiceV3 amexGroupRestTemplateService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RestTemplate restTemplate = new RestTemplate();
    @Mock
    private AmexMacHeaderUtilsV2 amexMacHeaderUtils;
    @Mock
    private Clock clock;

    @InjectMocks
    private AmexGroupAuthenticationServiceV3 amexGroupAuthenticationService;

    private Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();

    private AmexGroupAuthMeansV6 amexGroupAuthMeans =  new AmexGroupAuthMeansV6("1234653", "jghfshfjshfd", null, null);

    private TokenResponses tokenResponses = new TokenResponses();

    private TokenResponse tokenResponse = new TokenResponse();

    private static final String MAC_STRING = "MAC ID=\"gfFb4K8esqZgMpzwF9SXzKLCCbPYV8bR\",ts=\"1463772177193\",nonce=\"61129a8d-ca24-464b-8891-9251501d86f0\", mac=\"uzybzLPj3fD8eBZaBzb4E7pZs+l+IWS0w/w2wwsExdo=\"";
    private static final String ACCESS_MEANS = "{\"tokens\":[{\"expires_in\":7193,\"access_token\":\"access-token-to-revoke\",\"refresh_token\":\"THE-REFRESH-TOKEN\",\"token_type\":\"mac\",\"scope\":\"MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO\",\"scope\":\"33f48435-06ae-42e1-816a-b80653562a56\",\"scope\":\"hmac-sha-256\"}]}";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String GRANT_TYPE_REVOKE_TOKEN = "revoke";
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";


    @BeforeEach
    public void before() throws InvalidKeyException, NoSuchAlgorithmException {

        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "THE-CLIENT-ID"));
        authenticationMeans.put(CLIENT_SECRET, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SECRET_STRING.getType(), "THE-CLIENT-SECRET"));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificates()));

        tokenResponse.setExpiresIn(1356654674L);
        Set<TokenResponse> tokenResponseSet = new HashSet<>();
        tokenResponseSet.add(tokenResponse);
        tokenResponses.setTokens(tokenResponseSet);
        when(amexMacHeaderUtils.generateAuthMacToken(anyString(),anyString(),anyString())).thenReturn(MAC_STRING);
    }


    private String readCertificates() {
        try {
            URI fileURI = this.getClass()
                    .getClassLoader()
                    .getResource("certificates/yolt_certificate.pem")
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test
    public void shouldCallGenerateMacTokenWithGrantTypeAuthorizationCode() throws JsonProcessingException, InvalidKeyException, NoSuchAlgorithmException {
        //given
        when(amexGroupRestTemplateService.buildRestTemplate(any(), any())).thenReturn(restTemplate);
        UrlCreateAccessMeansRequest urlRequestAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setRedirectUrlPostedBackFromSite("http://yolt.com/callback")
                .setBaseClientRedirectUrl("http://yolt.com/callback")
                .setAuthenticationMeans(authenticationMeans)
                .setProviderState("STATE")
                .setState("STATE")
                .setPsuIpAddress("127.0.0.1")
                .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("String value for token respone");
        when(restTemplate.postForEntity(any(), any(), any(), (Class<Object>) any())).thenReturn(ResponseEntity.status(HttpStatus.OK).body(tokenResponse));
        when(Instant.now(clock)).thenReturn(Instant.now(Clock.systemUTC()));

        // when
        amexGroupAuthenticationService.createNewAccessMeansFromCsv(amexGroupAuthMeans, urlRequestAccessMeansRequest, "SOME_ACCESSCODE");

        // then
        verify(amexMacHeaderUtils).generateAuthMacToken(anyString(), anyString(), eq(GRANT_TYPE_AUTHORIZATION_CODE));
    }

    @Test
    public void shouldCallGenerateMacTokenWithGrantTypeRefreshToken() throws JsonProcessingException, InvalidKeyException, NoSuchAlgorithmException, TokenInvalidException {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(UUID.randomUUID(), ACCESS_MEANS, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .build();
        when(amexGroupRestTemplateService.buildRestTemplate(any(), any())).thenReturn(restTemplate);
        when(objectMapper.readValue(anyString(), (Class<TokenResponses>) any())).thenReturn(tokenResponses);
        when(objectMapper.writeValueAsString(any())).thenReturn("String value for token respone");
        when(restTemplate.postForEntity(any(), any(), any(), (Class<Object>) any())).thenReturn(ResponseEntity.status(HttpStatus.OK).body(tokenResponse));
        when(Instant.now(clock)).thenReturn(Instant.now(Clock.systemUTC()));

        // when
        amexGroupAuthenticationService.getRefreshAccessMeans(urlRefreshAccessMeansRequest, amexGroupAuthMeans);

        // then
        verify(amexMacHeaderUtils,times(1)).generateAuthMacToken(anyString(),anyString(),eq(GRANT_TYPE_REFRESH_TOKEN));
    }

    @Test
    public void shouldCallGenerateMacTokenWithGrantTypeRevokeToken() throws JsonProcessingException, TokenInvalidException, InvalidKeyException, NoSuchAlgorithmException {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(UUID.randomUUID(), ACCESS_MEANS, new Date(), new Date());
        UrlOnUserSiteDeleteRequest request = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("12345")
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(accessMeansDTO)
                .build();
        RevokeTokenResponse response = new RevokeTokenResponse();
        response.setResult("success");
        when(amexGroupRestTemplateService.buildRestTemplate(any(), any())).thenReturn(restTemplate);
        when(objectMapper.readValue(anyString(), (Class<TokenResponses>) any())).thenReturn(tokenResponses);
        when(restTemplate.postForEntity(any(), any(), any(), (Class<Object>) any())).thenReturn(ResponseEntity.status(HttpStatus.OK).body(response));

        // when
        amexGroupAuthenticationService.revokeUserToken(request,amexGroupAuthMeans);

        // then
        verify(amexMacHeaderUtils,times(1)).generateAuthMacToken(anyString(),anyString(),eq(GRANT_TYPE_REVOKE_TOKEN));
    }
}
