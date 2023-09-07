package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDataDomesticScheduledConsentResponse1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticScheduledConsentResponse1;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class YoltBankUkAuthorizationUrlScheduledPaymentExtractorTest {

    @InjectMocks
    private YoltBankUkAuthorizationUrlScheduledPaymentExtractor urlExtractor;

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
        String paymentId = "fakePaymentId";
        String redirectUrl = "http://localhost/redirect";
        String state = "fakeState";
        YoltBankUkInitiateScheduledPaymentPreExecutionResult yoltBankUkInitiateScheduledPaymentPreExecutionResult = new YoltBankUkInitiateScheduledPaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, signingKid, publicKid),
                signer,
                redirectUrl,
                state,
                paymentId,
                "http://yoltbank.io/authorize"
        );
        given(signer.sign(jsonWebSignatureArgumentCaptor.capture(), eq(signingKid), eq(SignatureAlgorithm.SHA256_WITH_RSA)))
                .willReturn(jwsSigningResult);
        String serialization = "serialization";
        given(jwsSigningResult.getCompactSerialization())
                .willReturn(serialization);

        // when
        String result = urlExtractor.extractAuthorizationUrl(null, yoltBankUkInitiateScheduledPaymentPreExecutionResult);

        // then
        JsonWebSignature capturedJsonWebSignature = jsonWebSignatureArgumentCaptor.getValue();
        JwtClaims jwtClaims = JwtClaims.parse(capturedJsonWebSignature.getUnverifiedPayload());
        assertThat(jwtClaims.hasClaim("iat")).isTrue();
        assertThat(jwtClaims.getClaimsMap(Set.of("iat")))
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "consent_id", paymentId,
                        "client_id", clientId.toString()
                ));
        assertThat(capturedJsonWebSignature.getKeyIdHeaderValue()).isEqualTo(publicKid.toString());
        assertThat(capturedJsonWebSignature.getAlgorithmHeaderValue()).isEqualTo(AlgorithmIdentifiers.RSA_USING_SHA256);
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(result).build();
        assertThat(uriComponents).extracting(UriComponents::getScheme, UriComponents::getHost, UriComponents::getPath)
                .contains("http", "yoltbank.io", "/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "client_id", clientId.toString(),
                        "token", serialization,
                        "redirect_uri", redirectUrl,
                        "state", state
                ));
    }

    @Test
    void shouldReturnAuthUrlApplyingConsentDataForExtractAuthorizationUrlWhenHttpResponseBodyIsNotNull() throws JsonProcessingException, InvalidJwtException {
        // given
        UUID signingKid = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        String redirectUrl = "http://localhost/redirect";
        String state = "fakeState";
        YoltBankUkInitiateScheduledPaymentPreExecutionResult yoltBankUkInitiateScheduledPaymentPreExecutionResult = new YoltBankUkInitiateScheduledPaymentPreExecutionResult(
                null,
                new PaymentAuthenticationMeans(clientId, signingKid, publicKid),
                signer,
                redirectUrl,
                state,
                null,
                null
        );
        String paymentConsent = "fakePaymentConsent";
        InitiatePaymentConsentResponse initiatePaymentConsentResponse = new InitiatePaymentConsentResponse(
                "http://yoltbank.io/authorize",
                paymentConsent
        );
        String paymentId = "fakePaymentId";
        OBWriteDomesticScheduledConsentResponse1 domesticConsentResponse1 = new OBWriteDomesticScheduledConsentResponse1()
                .data(new OBWriteDataDomesticScheduledConsentResponse1()
                        .consentId(paymentId));
        given(objectMapper.readValue(paymentConsent, OBWriteDomesticScheduledConsentResponse1.class))
                .willReturn(domesticConsentResponse1);
        given(signer.sign(jsonWebSignatureArgumentCaptor.capture(), eq(signingKid), eq(SignatureAlgorithm.SHA256_WITH_RSA)))
                .willReturn(jwsSigningResult);
        String serialization = "serialization";
        given(jwsSigningResult.getCompactSerialization())
                .willReturn(serialization);

        // when
        String result = urlExtractor.extractAuthorizationUrl(initiatePaymentConsentResponse, yoltBankUkInitiateScheduledPaymentPreExecutionResult);

        // then
        JsonWebSignature capturedJsonWebSignature = jsonWebSignatureArgumentCaptor.getValue();
        JwtClaims jwtClaims = JwtClaims.parse(capturedJsonWebSignature.getUnverifiedPayload());
        assertThat(jwtClaims.hasClaim("iat")).isTrue();
        assertThat(jwtClaims.getClaimsMap(Set.of("iat")))
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "consent_id", paymentId,
                        "client_id", clientId.toString()
                ));
        assertThat(capturedJsonWebSignature.getKeyIdHeaderValue()).isEqualTo(publicKid.toString());
        assertThat(capturedJsonWebSignature.getAlgorithmHeaderValue()).isEqualTo(AlgorithmIdentifiers.RSA_USING_SHA256);
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(result).build();
        assertThat(uriComponents).extracting(UriComponents::getScheme, UriComponents::getHost, UriComponents::getPath)
                .contains("http", "yoltbank.io", "/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "client_id", clientId.toString(),
                        "token", serialization,
                        "redirect_uri", redirectUrl,
                        "state", state
                ));
    }
}
