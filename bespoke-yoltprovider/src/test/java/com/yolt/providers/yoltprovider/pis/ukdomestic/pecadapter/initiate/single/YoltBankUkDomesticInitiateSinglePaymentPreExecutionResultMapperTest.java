package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.UuidType;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.YoltBankPaymentRequestBodyValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class YoltBankUkDomesticInitiateSinglePaymentPreExecutionResultMapperTest {

    @InjectMocks
    private YoltBankUkDomesticInitiateSinglePaymentPreExecutionResultMapper preExecutionResultMapper;

    @Mock
    private YoltBankPaymentRequestBodyValidator validator;

    @Test
    void shouldReturnUkInitPreExecutionResultForMapWhenCorrectData() {
        // given
        doNothing().when(validator).validate(any());
        InitiateUkDomesticPaymentRequestDTO paymentRequestDTO = mock(InitiateUkDomesticPaymentRequestDTO.class);
        Signer signer = mock(Signer.class);
        UUID clientId = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        UUID signingKid = UUID.randomUUID();
        InitiateUkDomesticPaymentRequest initiateUkDomesticPaymentRequest = new InitiateUkDomesticPaymentRequest(
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
        YoltBankUkInitiateSinglePaymentPreExecutionResult result = preExecutionResultMapper.map(initiateUkDomesticPaymentRequest);

        // then
        assertThat(result).isEqualTo(new YoltBankUkInitiateSinglePaymentPreExecutionResult(
                paymentRequestDTO,
                new PaymentAuthenticationMeans(clientId, signingKid, publicKid),
                signer,
                "fakeBaseClientRedirectUrl",
                "fakeState",
                null,
                null
        ));
    }

    private Map<String, BasicAuthenticationMean> prepareAuthMeans(UUID clientId, UUID publicKid, UUID signingKid) {
        return Map.of(
                CLIENT_ID, new BasicAuthenticationMean(UuidType.getInstance(), clientId.toString()),
                PUBLIC_KID, new BasicAuthenticationMean(UuidType.getInstance(), publicKid.toString()),
                PRIVATE_KID, new BasicAuthenticationMean(UuidType.getInstance(), signingKid.toString())
        );
    }
}