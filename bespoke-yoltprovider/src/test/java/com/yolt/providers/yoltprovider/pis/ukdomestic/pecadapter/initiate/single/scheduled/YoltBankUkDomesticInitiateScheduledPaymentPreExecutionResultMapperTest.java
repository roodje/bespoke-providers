package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.UuidType;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class YoltBankUkDomesticInitiateScheduledPaymentPreExecutionResultMapperTest {

    private YoltBankUkDomesticInitiateScheduledPaymentPreExecutionResultMapper preExecutionResultMapper;

    @BeforeEach
    public void setup() {
        preExecutionResultMapper = new YoltBankUkDomesticInitiateScheduledPaymentPreExecutionResultMapper();
    }

    @Test
    void shouldReturnUkInitPreExecutionResultForMapWhenCorrectData() {
        // given
        InitiateUkDomesticScheduledPaymentRequestDTO paymentRequestDTO = exampleRequestDTO();
        Signer signer = mock(Signer.class);
        UUID clientId = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        UUID signingKid = UUID.randomUUID();
        InitiateUkDomesticScheduledPaymentRequest initiateUkDomesticPaymentRequest = new InitiateUkDomesticScheduledPaymentRequest(
                paymentRequestDTO,
                "fakeBaseClientRedirectUrl",
                "fakeState",
                prepareAuthMeans(clientId, publicKid, signingKid),
                signer,
                null,
                null,
                null
        );

        // when
        YoltBankUkInitiateScheduledPaymentPreExecutionResult result = preExecutionResultMapper.map(initiateUkDomesticPaymentRequest);

        // then
        assertThat(result).isEqualTo(new YoltBankUkInitiateScheduledPaymentPreExecutionResult(
                paymentRequestDTO,
                new PaymentAuthenticationMeans(clientId, signingKid, publicKid),
                signer,
                "fakeBaseClientRedirectUrl",
                "fakeState",
                null,
                null
        ));
    }

    private InitiateUkDomesticScheduledPaymentRequestDTO exampleRequestDTO() {
        return new InitiateUkDomesticScheduledPaymentRequestDTO(null, null, null, null, null, null, null, null);
    }

    private Map<String, BasicAuthenticationMean> prepareAuthMeans(UUID clientId, UUID publicKid, UUID signingKid) {
        return Map.of(
                CLIENT_ID, new BasicAuthenticationMean(UuidType.getInstance(), clientId.toString()),
                PUBLIC_KID, new BasicAuthenticationMean(UuidType.getInstance(), publicKid.toString()),
                PRIVATE_KID, new BasicAuthenticationMean(UuidType.getInstance(), signingKid.toString())
        );
    }
}