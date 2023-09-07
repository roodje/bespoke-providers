package com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.starlingbank.SampleAuthenticationMeans;
import com.yolt.providers.starlingbank.TestSigner;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankUkPaymentInitiatePaymentPreExecutorResultMapperTest {

    @InjectMocks
    private StarlingBankInitiatePaymentPreExecutorResultMapper preExecutorResultMapper;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private TestSigner signer;

    @Test
    void shouldReturnUkPaymentPreExecutorResultForUkDomesticPaymentRequest() throws IOException, URISyntaxException {
        // given
        InitiateUkDomesticPaymentRequest submitPaymentRequest = new InitiateUkDomesticPaymentRequest(
                createUkPaymentRequest(),
                null,
                null,
                new SampleAuthenticationMeans().getAuthenticationMeans(),
                signer,
                restTemplateManager,
                null,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID())
        );

        // when
        StarlingBankInitiatePaymentExecutionContextPreExecutionResult result = preExecutorResultMapper.map(submitPaymentRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProviderStatePayload().getExternalPaymentId()).isNotNull();
        assertThat(result.getProviderStatePayload().getPaymentRequest()).extracting(
                InitiateUkDomesticPaymentRequestDTO::getEndToEndIdentification,
                InitiateUkDomesticPaymentRequestDTO::getRemittanceInformationUnstructured,
                InitiateUkDomesticPaymentRequestDTO::getAmount,
                initiateUkDomesticPaymentRequestDTO -> initiateUkDomesticPaymentRequestDTO.getCreditorAccount().getAccountIdentifier())
                .contains("endToEndIdentification",
                        "Payment reference unstructured",
                        new BigDecimal("123.11"),
                        "56666688888887");
    }

    private InitiateUkDomesticPaymentRequestDTO createUkPaymentRequest() {
        return new InitiateUkDomesticPaymentRequestDTO(
                "endToEndIdentification",
                CurrencyCode.PLN.name(),
                new BigDecimal("123.11"),
                new UkAccountDTO(
                        "56666688888887",
                        AccountIdentifierScheme.SORTCODEACCOUNTNUMBER,
                        "Michal Dziewanowski",
                        null),
                null,
                "Payment reference unstructured",
                Collections.singletonMap("remittanceInformationStructured", "reference"));
    }
}
