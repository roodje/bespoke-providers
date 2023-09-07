package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.yolt.providers.common.pis.common.PeriodicPaymentFrequency;
import com.yolt.providers.common.pis.common.UkPeriodicPaymentInfo;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPeriodicPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class YoltBankUkInitiatePeriodicPaymentHttpRequestBodyProviderTest {

    private YoltBankUkInitiatePeriodicPaymentHttpRequestBodyProvider httpRequestBodyProvider;

    @BeforeEach
    public void setup() {
        httpRequestBodyProvider = new YoltBankUkInitiatePeriodicPaymentHttpRequestBodyProvider();
    }

    @Test
    void shouldReturnOBWriteDomesticConsent1ForProvideHttpRequestBody() {
        // given
        UUID endToEndIdentification = UUID.randomUUID();
        InitiateUkDomesticPeriodicPaymentRequestDTO requestDTO = new InitiateUkDomesticPeriodicPaymentRequestDTO(
                endToEndIdentification.toString(),
                CurrencyCode.GBP.toString(),
                new BigDecimal("123.12"),
                new UkAccountDTO("123", AccountIdentifierScheme.IBAN, "Creditor", "321"),
                new UkAccountDTO("234", AccountIdentifierScheme.IBAN, "Debtor", "432"),
                "remittance",
                Map.of("remittanceInformationStructured", "Value"),
                new UkPeriodicPaymentInfo(
                        LocalDate.of(2000, 12, 12),
                        LocalDate.of(2020, 1, 1),
                        PeriodicPaymentFrequency.MONTHLY
                )
        );
        YoltBankUkInitiatePeriodicPaymentPreExecutionResult yoltBankUkInitiatePeriodicPaymentPreExecutionResult = new YoltBankUkInitiatePeriodicPaymentPreExecutionResult(
                requestDTO,
                new PaymentAuthenticationMeans(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                null,
                null,
                null,
                null,
                null
        );

        // when
        OBWriteDomesticStandingOrderConsent1 result = httpRequestBodyProvider.provideHttpRequestBody(yoltBankUkInitiatePeriodicPaymentPreExecutionResult);

        // then
        assertThat(result).isEqualTo(
                new OBWriteDomesticStandingOrderConsent1()
                        .risk(new OBRisk1()
                                .paymentContextCode(OBExternalPaymentContext1Code.PARTYTOPARTY))
                        .data(new OBWriteDataDomesticStandingOrderConsent1()
                                .initiation(new OBDomesticStandingOrder1()
                                        .frequency("MONTHLY")
                                        .firstPaymentDateTime(OffsetDateTime.of(2000, 12, 12, 12, 00, 00, 00, ZoneOffset.UTC))
                                        .finalPaymentDateTime(OffsetDateTime.of(2020, 01, 01, 12, 00, 00, 00, ZoneOffset.UTC))
                                        .firstPaymentAmount(new OBDomesticStandingOrder1FirstPaymentAmount()
                                                .amount("123.12")
                                                .currency("GBP"))
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
                                        .reference("Value")))
        );
    }
}