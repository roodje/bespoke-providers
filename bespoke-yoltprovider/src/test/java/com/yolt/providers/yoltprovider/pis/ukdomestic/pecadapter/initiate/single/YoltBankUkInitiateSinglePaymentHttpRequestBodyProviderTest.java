package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InstructionIdentificationProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YoltBankUkInitiateSinglePaymentHttpRequestBodyProviderTest {

    @InjectMocks
    private YoltBankUkInitiateSinglePaymentHttpRequestBodyProvider httpRequestBodyProvider;
    @Mock
    private InstructionIdentificationProvider instructionIdentificationProvider;

    @BeforeEach
    public void setup() {
        when(instructionIdentificationProvider.getInstructionIdentification()).thenReturn("instructedIdentification");
    }

    @Test
    void shouldReturnOBWriteDomesticConsent1ForProvideHttpRequestBody() {
        // given
        UUID endToEndIdentification = UUID.randomUUID();
        InitiateUkDomesticPaymentRequestDTO requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                endToEndIdentification.toString(),
                CurrencyCode.GBP.toString(),
                new BigDecimal("123.12"),
                new UkAccountDTO("123", AccountIdentifierScheme.IBAN, "Creditor", "321"),
                new UkAccountDTO("234", AccountIdentifierScheme.IBAN, "Debtor", "432"),
                "remittance",
                Collections.emptyMap()
        );
        YoltBankUkInitiateSinglePaymentPreExecutionResult yoltBankUkInitiateSinglePaymentPreExecutionResult = new YoltBankUkInitiateSinglePaymentPreExecutionResult(
                requestDTO,
                new PaymentAuthenticationMeans(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                null,
                null,
                null,
                null,
                null
        );

        // when
        OBWriteDomesticConsent1 result = httpRequestBodyProvider.provideHttpRequestBody(yoltBankUkInitiateSinglePaymentPreExecutionResult);

        // then

        assertThat(result).isEqualTo(
                new OBWriteDomesticConsent1()
                        .risk(new OBRisk1()
                                .paymentContextCode(OBExternalPaymentContext1Code.PARTYTOPARTY))
                        .data(new OBWriteDataDomesticConsent1()
                                .initiation(new OBDomestic1()
                                        .creditorAccount(new OBCashAccountCreditor2()
                                                .identification("123")
                                                .name("Creditor")
                                                .schemeName("UK.OBIE.IBAN")
                                                .secondaryIdentification("321"))
                                        .debtorAccount(new OBCashAccountDebtor3()
                                                .identification("234")
                                                .name("Debtor")
                                                .schemeName("UK.OBIE.IBAN")
                                                .secondaryIdentification("432"))
                                        .endToEndIdentification(endToEndIdentification.toString())
                                        .instructionIdentification("instructedIdentification")
                                        .instructedAmount(new OBDomestic1InstructedAmount()
                                                .amount("123.12")
                                                .currency("GBP"))
                                        .remittanceInformation(new OBRemittanceInformation1()
                                                .unstructured("remittance"))))
        );
    }

}