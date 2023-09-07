package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDataDomesticConsentResponse1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsentResponse1;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class YoltBankUkAuthorizationUrlSinglePaymentExtractorTest {

    @InjectMocks
    private YoltBankUkAuthorizationUrlSinglePaymentExtractor urlExtractor;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Signer signer;

    @Mock
    private JwsSigningResult jwsSigningResult;

    @Captor
    private ArgumentCaptor<JsonWebSignature> jsonWebSignatureArgumentCaptor;

    @Test
    void shouldReturnAuthorizationUrlCreatedBasedOnPreExecutionResultForExtractAuthorizationUrlWhenHttpResponseBodyIsNull() throws InvalidJwtException {
        // given
        UUID signingKid = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        YoltBankUkInitiateSinglePaymentPreExecutionResult yoltBankUkInitiateSinglePaymentPreExecutionResult = new YoltBankUkInitiateSinglePaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, signingKid, publicKid),
                signer,
                "http://localhost/redirect",
                "fakeState",
                "fakePaymentId",
                "http://yoltbank.io/authorize"
        );
        given(signer.sign(jsonWebSignatureArgumentCaptor.capture(), eq(signingKid), eq(SignatureAlgorithm.SHA256_WITH_RSA)))
                .willReturn(jwsSigningResult);
        given(jwsSigningResult.getCompactSerialization())
                .willReturn("fakeJws");

        // when
        String result = urlExtractor.extractAuthorizationUrl(null, yoltBankUkInitiateSinglePaymentPreExecutionResult);

        // then
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(result).build();
        assertThat(uriComponents).extracting(UriComponents::getScheme, UriComponents::getHost, UriComponents::getPath)
                .contains("http", "yoltbank.io", "/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "client_id", clientId.toString(),
                        "token", "fakeJws",
                        "redirect_uri", "http://localhost/redirect",
                        "state", "fakeState"
                ));
    }

    @Test
    void shouldUseProperlyCreatedSignatureForExtractAuthorizationUrl() throws InvalidJwtException {
        // given
        UUID clientId = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        YoltBankUkInitiateSinglePaymentPreExecutionResult yoltBankUkInitiateSinglePaymentPreExecutionResult = new YoltBankUkInitiateSinglePaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, UUID.randomUUID(), publicKid),
                signer,
                "http://localhost/redirect",
                "fakeState",
                "fakePaymentId",
                "http://yoltbank.io/authorize"
        );
        given(signer.sign(jsonWebSignatureArgumentCaptor.capture(), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(jwsSigningResult);
        given(jwsSigningResult.getCompactSerialization())
                .willReturn("fakeJws");

        // when
        urlExtractor.extractAuthorizationUrl(null, yoltBankUkInitiateSinglePaymentPreExecutionResult);

        // then
        JsonWebSignature capturedJsonWebSignature = jsonWebSignatureArgumentCaptor.getValue();
        JwtClaims jwtClaims = JwtClaims.parse(capturedJsonWebSignature.getUnverifiedPayload());
        assertThat(jwtClaims.hasClaim("iat")).isTrue();
        assertThat(jwtClaims.getClaimsMap(Set.of("iat")))
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "consent_id", "fakePaymentId",
                        "client_id", clientId.toString()
                ));
        assertThat(capturedJsonWebSignature.getKeyIdHeaderValue()).isEqualTo(publicKid.toString());
        assertThat(capturedJsonWebSignature.getAlgorithmHeaderValue()).isEqualTo(AlgorithmIdentifiers.RSA_USING_SHA256);
    }

    @Test
    void shouldReturnAuthorizationUrlApplyingConsentDataForExtractAuthorizationUrlWhenHttpResponseBodyIsNotNull() throws JsonProcessingException, InvalidJwtException {
        // given
        UUID signingKid = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        String baseClientRedirectUrl = "http://localhost/redirect";
        String state = "fakeState";
        YoltBankUkInitiateSinglePaymentPreExecutionResult yoltBankUkInitiateSinglePaymentPreExecutionResult = new YoltBankUkInitiateSinglePaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, signingKid, publicKid),
                signer,
                baseClientRedirectUrl,
                state,
                null,
                null
        );
        String fakePaymentConsent = "fakePaymentConsent";
        InitiatePaymentConsentResponse initiatePaymentConsentResponse = new InitiatePaymentConsentResponse(
                "http://yoltbank.io/authorize",
                fakePaymentConsent
        );
        String paymentId = "fakePaymentId";
        OBWriteDomesticConsentResponse1 domesticConsentResponse1 = new OBWriteDomesticConsentResponse1()
                .data(new OBWriteDataDomesticConsentResponse1()
                        .consentId(paymentId));
        given(objectMapper.readValue(fakePaymentConsent, OBWriteDomesticConsentResponse1.class))
                .willReturn(domesticConsentResponse1);
        given(signer.sign(jsonWebSignatureArgumentCaptor.capture(), eq(signingKid), eq(SignatureAlgorithm.SHA256_WITH_RSA)))
                .willReturn(jwsSigningResult);
        String serialization = "serialization";
        given(jwsSigningResult.getCompactSerialization())
                .willReturn(serialization);

        // when
        String result = urlExtractor.extractAuthorizationUrl(initiatePaymentConsentResponse, yoltBankUkInitiateSinglePaymentPreExecutionResult);

        // then
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(result).build();
        assertThat(uriComponents).extracting(UriComponents::getScheme, UriComponents::getHost, UriComponents::getPath)
                .contains("http", "yoltbank.io", "/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "client_id", clientId.toString(),
                        "token", serialization,
                        "redirect_uri", baseClientRedirectUrl,
                        "state", state
                ));
    }
}
