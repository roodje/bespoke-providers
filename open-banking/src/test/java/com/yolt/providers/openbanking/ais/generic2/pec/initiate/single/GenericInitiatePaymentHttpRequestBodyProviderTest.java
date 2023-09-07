package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.DataMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericInitiatePaymentHttpRequestBodyProviderTest {

    private GenericInitiatePaymentHttpRequestBodyProvider subject;

    @Mock
    private DataMapper<OBWriteDomestic2DataInitiation, InitiateUkDomesticPaymentRequestDTO> dataInitiationMapper;

    @BeforeEach
    void beforeEach() {
        subject = new GenericInitiatePaymentHttpRequestBodyProvider(dataInitiationMapper, OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY);
    }

    @Test
    void shouldReturnWriteDomesticConsentWhenCorrectDataIsProvided() {
        // given
        InitiateUkDomesticPaymentRequestDTO aymentRequest = preparePayment();
        GenericInitiatePaymentPreExecutionResult preExecutionResult = new GenericInitiatePaymentPreExecutionResult();
        preExecutionResult.setPaymentRequestDTO(aymentRequest);
        OBWriteDomestic2DataInitiation dataInitiation = new OBWriteDomestic2DataInitiation();

        given(dataInitiationMapper.map(any(InitiateUkDomesticPaymentRequestDTO.class)))
                .willReturn(dataInitiation);

        // when
        OBWriteDomesticConsent4 result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        then(dataInitiationMapper)
                .should()
                .map(aymentRequest);

        assertThat(result.getRisk()).satisfies(obRisk1 -> assertThat(obRisk1.getPaymentContextCode()).isEqualTo(OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY));
        assertThat(result.getData()).satisfies(data -> assertThat(data.getInitiation()).isEqualTo(dataInitiation));
    }

    private InitiateUkDomesticPaymentRequestDTO preparePayment() {
        return new InitiateUkDomesticPaymentRequestDTO(
                "endToEndIdentification",
                CurrencyCode.GBP.toString(),
                new BigDecimal("100.00"),
                new UkAccountDTO("creditorIban", AccountIdentifierScheme.IBAN, "creditor", "creditor2Iban"),
                new UkAccountDTO("debtorIban", AccountIdentifierScheme.IBAN, "debtor", "debtor2Iban"),
                "remittanceUnstructured",
                Collections.emptyMap()
        );
    }
}