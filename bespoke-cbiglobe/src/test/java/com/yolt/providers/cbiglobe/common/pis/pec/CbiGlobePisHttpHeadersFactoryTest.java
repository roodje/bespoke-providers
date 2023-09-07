package com.yolt.providers.cbiglobe.common.pis.pec;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeSigningUtil;
import com.yolt.providers.cbiglobe.common.util.HttpUtils;
import com.yolt.providers.common.cryptography.Signer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CbiGlobePisHttpHeadersFactoryTest {

    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    private static final String ASPSP_PRODUCT_CODE_HEADER = "aspsp-product-code";
    private static final String TPP_REDIRECT_URI_HEADER = "tpp-redirect-uri";
    private static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    private static final String ASPSP_CODE_HEADER = "aspsp-code";
    private static final String DIGEST_HEADER = "digest";
    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String DATE_HEADER = "date";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER = "tpp-signature-certificate";
    private static final String SIGNATURE_HEADER = "signature";

    @InjectMocks
    private CbiGlobePisHttpHeadersFactory subject;

    @Mock
    private Signer signer;

    @Test
    void shouldReturnProperHttpHeadersForCreatePaymentInitiationHttpHeadersWhenCorrectData() {
        // given
        var authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var cbiGlobeAuthenticationMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(authenticationMeans, "CBI_GLOBE");
        var accessToken = "accessToken";
        var aspspData = new AspspData();
        aspspData.setCode("CODE");
        aspspData.setProductCode("PRODUCT-CODE");
        var signatureData = cbiGlobeAuthenticationMeans.getSigningData(signer);
        var psuIpAddress = "psuIpAddress";
        var redirectUrlWithState = "https://www.yolt.com?state=state";
        var initiatePaymentRequest = new InitiatePaymentRequest();

        // when
        var result = subject.createPaymentInitiationHttpHeaders(accessToken, aspspData, signatureData, psuIpAddress,
                redirectUrlWithState, initiatePaymentRequest, Clock.systemUTC());

        // then
        assertThat(result.toSingleValueMap()).containsOnlyKeys(
                PSU_IP_ADDRESS_HEADER,
                ASPSP_PRODUCT_CODE_HEADER,
                TPP_REDIRECT_URI_HEADER,
                TPP_REDIRECT_PREFERRED_HEADER,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.AUTHORIZATION,
                ASPSP_CODE_HEADER,
                DIGEST_HEADER,
                X_REQUEST_ID_HEADER,
                DATE_HEADER,
                TPP_SIGNATURE_CERTIFICATE_HEADER,
                SIGNATURE_HEADER
        );

        assertThat(result.toSingleValueMap()).containsAllEntriesOf(Map.of(
                ASPSP_PRODUCT_CODE_HEADER, aspspData.getProductCode(),
                PSU_IP_ADDRESS_HEADER, psuIpAddress,
                TPP_REDIRECT_URI_HEADER, redirectUrlWithState,
                TPP_REDIRECT_PREFERRED_HEADER, "true",
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.AUTHORIZATION, "Bearer " + accessToken,
                ASPSP_CODE_HEADER, aspspData.getCode(),
                DIGEST_HEADER, CbiGlobeSigningUtil.getDigest(initiatePaymentRequest)
        ));
    }

    @Test
    void shouldReturnProperHttpHeadersForCreatingPaymentStatusHeadersWhenCorrectData() {
        // given
        var authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var cbiGlobeAuthenticationMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(authenticationMeans, "CBI_GLOBE");
        var accessToken = "accessToken";
        var aspspData = new AspspData();
        aspspData.setCode("CODE");
        aspspData.setProductCode("PRODUCT-CODE");
        var signatureData = cbiGlobeAuthenticationMeans.getSigningData(signer);

        // when
        var result = subject.createPaymentStatusHeaders(accessToken, aspspData, signatureData, Clock.systemUTC());

        // then
        assertThat(result.toSingleValueMap()).containsOnlyKeys(
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.AUTHORIZATION,
                ASPSP_CODE_HEADER,
                DIGEST_HEADER,
                X_REQUEST_ID_HEADER,
                DATE_HEADER,
                TPP_SIGNATURE_CERTIFICATE_HEADER,
                SIGNATURE_HEADER
        );

        assertThat(result.toSingleValueMap()).containsAllEntriesOf(Map.of(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.AUTHORIZATION, "Bearer " + accessToken,
                ASPSP_CODE_HEADER, aspspData.getCode(),
                DIGEST_HEADER, CbiGlobeSigningUtil.getDigest(new byte[]{})
        ));
    }

    @Test
    void shouldReturnProperHttpHeadersForCreateClientCredentialsHeadersWhenCorrectData() {
        // given
        var clientId = "fakeClientId";
        var clientSecret = "fakeClientSecret";

        // when
        var result = subject.createClientCredentialsHeaders(clientId, clientSecret);

        // then
        assertThat(result.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                HttpHeaders.AUTHORIZATION, HttpUtils.basicCredentials(clientId, clientSecret)
        ));
    }
}