package com.yolt.providers.openbanking.ais.revolutgroup.revolut.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.revolutgroup.RevolutSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.revolutgroup.common.RevolutPropertiesV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutGbAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding.CertificateDistinguishedNameResponse;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding.RevolutAutoOnboardingResponse;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.openbanking.ais.revolutgroup.revolut.service.RevolutAutoOnboardingServiceV3.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RevolutAutoOnboardingServiceV3Test {

    private static final Instant CLOCK_INSTANT = Instant.parse("2020-01-08T07:00:00.00Z");

    @Mock
    private RevolutPropertiesV2 properties;
    @Mock
    private Clock clock;
    @Mock
    private Signer signer;
    @Mock
    private HttpClient httpClient;
    @Mock
    private JwsSigningResult jwsSigningResult;

    @Captor
    private ArgumentCaptor<JsonWebSignature> jwsArgumentCaptor;
    @Captor
    private ArgumentCaptor<HttpEntity<String>> httpEntityArgumentCaptor;

    private RevolutAutoOnboardingServiceV3 subject;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws Exception {
        authenticationMeans = new RevolutSampleAuthenticationMeans().getAuthenticationMeansNewOBCerts();

        when(clock.instant())
                .thenReturn(CLOCK_INSTANT);

        when(properties.getAudience())
                .thenReturn("audience");

        when(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .thenReturn(jwsSigningResult);
        when(jwsSigningResult.getCompactSerialization())
                .thenReturn("jws");
    }

    @Test
    void shouldSendProperRegistrationRequestWithJwsAndReturnCorrectResponseForRegisterWhenCorrectData() throws TokenInvalidException, InvalidJwtException, MalformedClaimException {
        // given
        subject = new RevolutAutoOnboardingServiceV3(properties, clock, RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME);
        authenticationMeans.remove(RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME);

        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRedirectUrls(Collections.singletonList("http://localhost/redirect"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .setSigner(signer)
                .build();

        DefaultAuthMeans defaultAuthMeans = RevolutGbAuthMeansBuilderV2.createAuthenticationMeans(authenticationMeans);
        JwtClaims expectedJwtClaims = createExpectedJwtClaims(CLOCK_INSTANT);
        JsonWebSignature expectedJws = createExpectedJws(expectedJwtClaims);

        RevolutAutoOnboardingResponse autoOnboardingResponse = new RevolutAutoOnboardingResponse();
        autoOnboardingResponse.setClientId("newClientId");

        CertificateDistinguishedNameResponse certificateDistinguishedNameResponse = new CertificateDistinguishedNameResponse();
        certificateDistinguishedNameResponse.setTlsClientAuthDn("tlsClientAuthDn");

        when(httpClient.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), anyString(), eq(RevolutAutoOnboardingResponse.class)))
                .thenReturn(ResponseEntity.ok(autoOnboardingResponse));
        when(httpClient.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), anyString(), eq(CertificateDistinguishedNameResponse.class)))
                .thenReturn(ResponseEntity.ok(certificateDistinguishedNameResponse));
        when(properties.getRegistrationUrl())
                .thenReturn("http://localhost/register");
        when(properties.getOAuthTokenUrl())
                .thenReturn("http://localhost/token");

        // when
        Optional<RevolutAutoOnboardingResponse> result = subject.register(httpClient, request, defaultAuthMeans);

        // then
        assertThat(result).hasValueSatisfying(response -> assertThat(response).isEqualToComparingFieldByField(autoOnboardingResponse));

        verify(signer).sign(jwsArgumentCaptor.capture(), eq(defaultAuthMeans.getSigningPrivateKeyId()), eq(SignatureAlgorithm.SHA256_WITH_RSA_PSS));
        JsonWebSignature capturedJws = jwsArgumentCaptor.getValue();
        assertThat(capturedJws).isEqualToComparingOnlyGivenFields(expectedJws, "algorithmHeaderValue", "keyIdHeaderValue", "payloadBytes");

        JwtClaims jwtClaims = JwtClaims.parse(capturedJws.getUnverifiedPayload());
        assertThat(jwtClaims).isEqualToIgnoringNullFields(expectedJwtClaims);

        verify(httpClient).exchange(
                eq("http://localhost/register"),
                eq(HttpMethod.POST),
                httpEntityArgumentCaptor.capture(),
                eq("register"),
                eq(RevolutAutoOnboardingResponse.class));

        verify(httpClient).exchange(
                eq("http://localhost/distinguished-name"),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq("get_certificate_dn"),
                eq(CertificateDistinguishedNameResponse.class));

        HttpEntity<String> capturedHttpEntity = httpEntityArgumentCaptor.getValue();
        HttpHeaders headers = capturedHttpEntity.getHeaders();
        assertThat(headers).hasSize(2);
        assertThat(headers.getAccept()).isEqualTo(Collections.singletonList(MediaType.APPLICATION_JSON));
        assertThat(headers.getContentType()).isEqualTo(MediaType.parseMediaType("application/jwt"));
        String body = capturedHttpEntity.getBody();
        assertThat(body).isEqualTo("jws");
    }

    private JsonWebSignature createExpectedJws(JwtClaims jwtClaims) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        jws.setKeyIdHeaderValue(RevolutSampleAuthenticationMeans.TEST_SIGNING_KEY_HEADER_ID);
        jws.setPayload(jwtClaims.toJson());
        return jws;
    }

    private JwtClaims createExpectedJwtClaims(Instant clockInstant) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(RevolutSampleAuthenticationMeans.TEST_SOFTWARE_ID);
        claims.setIssuedAt(NumericDate.fromMilliseconds(clockInstant.toEpochMilli()));
        claims.setExpirationTime(NumericDate.fromMilliseconds(clockInstant.plus(1, ChronoUnit.HOURS).toEpochMilli()));
        claims.setAudience(properties.getAudience());
        claims.setClaim(SCOPE_CLAIM, List.of("openid", "accounts", "payments"));
        claims.setClaim(REDIRECT_URIS_CLAIM, Collections.singletonList("http://localhost/redirect"));
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM, TOKEN_ENDPOINT_AUTH_METHOD);
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(TLS_CLIENT_AUTH_DN_CLAIM, "tlsClientAuthDn");
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, RevolutSampleAuthenticationMeans.TEST_SSA);
        return claims;
    }
}
