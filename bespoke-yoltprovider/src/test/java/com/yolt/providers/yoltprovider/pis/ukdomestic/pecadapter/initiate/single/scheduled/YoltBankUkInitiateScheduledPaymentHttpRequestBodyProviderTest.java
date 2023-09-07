package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YoltBankUkInitiateScheduledPaymentHttpRequestBodyProviderTest {

    @InjectMocks
    private YoltBankUkInitiateScheduledPaymentHttpRequestBodyProvider httpRequestBodyProvider;
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
        InitiateUkDomesticScheduledPaymentRequestDTO requestDTO = new InitiateUkDomesticScheduledPaymentRequestDTO(
                endToEndIdentification.toString(),
                CurrencyCode.GBP.toString(),
                new BigDecimal("123.12"),
                new UkAccountDTO("123", AccountIdentifierScheme.IBAN, "Creditor", "321"),
                new UkAccountDTO("234", AccountIdentifierScheme.IBAN, "Debtor", "432"),
                "remittance",
                Collections.emptyMap(),
                OffsetDateTime.of(2000, 12, 12, 01, 01, 01, 01, ZoneOffset.UTC)
        );
        YoltBankUkInitiateScheduledPaymentPreExecutionResult yoltBankUkInitiateScheduledPaymentPreExecutionResult = new YoltBankUkInitiateScheduledPaymentPreExecutionResult(
                requestDTO,
                new PaymentAuthenticationMeans(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                null,
                null,
                null,
                null,
                null
        );

        // when
        OBWriteDomesticScheduledConsent1 result = httpRequestBodyProvider.provideHttpRequestBody(yoltBankUkInitiateScheduledPaymentPreExecutionResult);

        // then
        assertThat(result).isEqualTo(
                new OBWriteDomesticScheduledConsent1()
                        .risk(new OBRisk1()
                                .paymentContextCode(OBExternalPaymentContext1Code.PARTYTOPARTY))
                        .data(new OBWriteDataDomesticScheduledConsent1()
                                .initiation(new OBDomesticScheduled1()
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
                                        .requestedExecutionDateTime(OffsetDateTime.of(2000, 12, 12, 01, 01, 01, 01, ZoneOffset.UTC))
                                        .instructedAmount(new OBDomestic1InstructedAmount()
                                                .amount("123.12")
                                                .currency("GBP"))
                                        .remittanceInformation(new OBRemittanceInformation1()
                                                .unstructured("remittance"))))
        );
    }
}