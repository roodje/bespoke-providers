package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.DataMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsent4;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsent4Data;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericInitiateScheduledPaymentHttpRequestBodyProviderTest {

    private long ONE_DAY_IN_SECONDS = 86400;
    private Clock clock = Clock.systemUTC();
    private GenericInitiateScheduledPaymentHttpRequestBodyProvider subject;

    @Mock
    private DataMapper<OBWriteDomesticScheduled2DataInitiation, InitiateUkDomesticScheduledPaymentRequestDTO> dataInitiationMapper;

    @BeforeEach
    void beforeEach() {
        subject = new GenericInitiateScheduledPaymentHttpRequestBodyProvider(dataInitiationMapper, OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY);
    }

    @Test
    void shouldReturnWriteDomesticConsentWhenCorrectDataIsProvided() {
        // given
        InitiateUkDomesticScheduledPaymentRequestDTO payment = preparePayment();
        GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult = new GenericInitiateScheduledPaymentPreExecutionResult();
        preExecutionResult.setPaymentRequestDTO(payment);
        OBWriteDomesticScheduled2DataInitiation dataInitiation = new OBWriteDomesticScheduled2DataInitiation();
        OBWriteDomesticScheduledConsent4 expectedMappedObject = new OBWriteDomesticScheduledConsent4()
                .risk(new OBRisk1().paymentContextCode(OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY))
                .data(new OBWriteDomesticScheduledConsent4Data()
                        .permission(OBWriteDomesticScheduledConsent4Data.PermissionEnum.CREATE)
                        .initiation(dataInitiation));
        given(dataInitiationMapper.map(payment))
                .willReturn(dataInitiation);

        // when
        OBWriteDomesticScheduledConsent4 result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result).isEqualTo(expectedMappedObject);
    }

    private InitiateUkDomesticScheduledPaymentRequestDTO preparePayment() {
        return new InitiateUkDomesticScheduledPaymentRequestDTO(
                "endToEndIdentification",
                CurrencyCode.GBP.toString(),
                new BigDecimal("100.00"),
                new UkAccountDTO("creditorIban", AccountIdentifierScheme.IBAN, "creditor", "creditor2Iban"),
                new UkAccountDTO("debtorIban", AccountIdentifierScheme.IBAN, "debtor", "debtor2Iban"),
                "remittanceUnstructured",
                Collections.emptyMap(),
                OffsetDateTime.now(clock).plusSeconds(ONE_DAY_IN_SECONDS)
        );
    }
}