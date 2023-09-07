package com.yolt.providers.knabgroup.common.data;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.knabgroup.common.auth.KnabSigningService;
import com.yolt.providers.knabgroup.common.auth.SignatureData;
import com.yolt.providers.knabgroup.common.dto.internal.KnabAccessMeans;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.DATE;

@ExtendWith(MockitoExtension.class)
public class KnabFetchDataHeadersTest {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    private static final String X_REQUEST_ID = "X-Request-ID";
    private static final String DIGEST = "Digest";
    private static final String SIGNATURE = "Signature";
    private static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String CONSENT_ID = "Consent-ID";
    private static final String AUTHORIZATION = "Authorization";

    private KnabFetchDataHeaders headers;

    @Mock
    private KnabSigningService signingService;
    @Mock
    private KnabAccessMeans accessMeans;
    @Mock
    private SignatureData signatureData;

    @Test
    public void shouldCreateProperHeadersWithPsuIpAddress() throws TokenInvalidException {
        //given
        when(signingService.calculateDigest(any())).thenReturn("digest");
        when(signingService.calculateSignature(any(KnabFetchDataHeaders.class), eq(signatureData), any(List.class))).thenReturn("signature");
        when(accessMeans.getAccessToken()).thenReturn("accessToken");
        when(accessMeans.getScope()).thenReturn("AIS:consentId");
        when(signatureData.getSigningCertificateInBase64()).thenReturn("base64Certificate");
        Instant currentTime = Instant.now();

        //when
        headers = new KnabFetchDataHeaders(signingService, currentTime, accessMeans, signatureData, Optional.of("psuIp"));

        //then
        assertThat(headers.toSingleValueMap()).hasSize(8);
        assertThat(headers.get(X_REQUEST_ID)).isNotNull();
        assertThat(headers.get(AUTHORIZATION)).containsExactly("Bearer accessToken");
        assertThat(headers.get(DIGEST)).containsExactly("digest");
        assertThat(headers.get(SIGNATURE)).containsExactly("signature");
        assertThat(headers.get(TPP_SIGNATURE_CERTIFICATE)).containsExactly("base64Certificate");
        assertThat(headers.get(PSU_IP_ADDRESS)).containsExactly("psuIp");
        assertThat(headers.get(CONSENT_ID)).containsExactly("consentId");
        assertThat(headers.get(DATE)).containsExactly(DATETIME_FORMATTER.format(currentTime));
    }

    @Test
    public void shouldThrowTokenInvalidExceptionWhenConsentIdIsMissing() {
        //given
        when(signingService.calculateDigest(any())).thenReturn("digest");
        when(accessMeans.getAccessToken()).thenReturn("accessToken");
        when(accessMeans.getScope()).thenReturn("offline_access");
        when(signatureData.getSigningCertificateInBase64()).thenReturn("base64Certificate");
        Instant currentTime = Instant.now();

        //when
        ThrowableAssert.ThrowingCallable call = () -> new KnabFetchDataHeaders(signingService, currentTime, accessMeans, signatureData, Optional.of("psuIp"));

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("Missing consentId in scope of user accessToken");
    }
}