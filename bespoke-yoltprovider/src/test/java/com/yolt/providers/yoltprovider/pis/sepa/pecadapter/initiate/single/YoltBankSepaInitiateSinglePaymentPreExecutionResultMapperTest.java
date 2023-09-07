package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.single;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.TestPaymentAuthMeansUtil;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.YoltBankSepaInitiatePaymentPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class YoltBankSepaInitiateSinglePaymentPreExecutionResultMapperTest {

    private YoltBankSepaInitiateSinglePaymentPreExecutionResultMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new YoltBankSepaInitiateSinglePaymentPreExecutionResultMapper();
    }

    @Test
    void shouldReturnSepaInitPreExecutionResultForMap() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder().build();
        Signer signer = mock(Signer.class);
        String fakeBaseClientRedirectUrl = "fakeBaseClientRedirectUrl";
        String fakeState = "fakeState";
        UUID clientId = UUID.randomUUID();
        UUID privateKid = UUID.randomUUID();
        UUID publicKid = UUID.randomUUID();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequest(
                requestDTO,
                fakeBaseClientRedirectUrl,
                fakeState,
                TestPaymentAuthMeansUtil.getBasicAuthMeans(clientId, publicKid, privateKid),
                signer,
                null,
                "",
                null
        );
        PaymentAuthenticationMeans paymentAuthenticationMeans = new PaymentAuthenticationMeans(clientId, privateKid, publicKid);

        // when
        YoltBankSepaInitiatePaymentPreExecutionResult result = mapper.map(initiatePaymentRequest);

        // then
        assertThat(result).isEqualToIgnoringGivenFields(
                new YoltBankSepaInitiatePaymentPreExecutionResult(
                        requestDTO,
                        paymentAuthenticationMeans,
                        signer,
                        fakeBaseClientRedirectUrl,
                        fakeState));
    }
}
