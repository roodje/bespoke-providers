package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.pis.uk.InitiateUkDomesticPaymentRequestBuilder;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.common.providerdetail.dto.DynamicFieldNames;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SepaInitiateSinglePaymentRequestMapperTest {

    private final SepaInitiateSinglePaymentRequestMapper subject = new SepaInitiateSinglePaymentRequestMapper(new SepaDynamicFieldsMapper());
    private final Signer signer = mock(Signer.class);
    private final RestTemplateManager restTemplateManager = mock(RestTemplateManager.class);

    @Test
    void shouldMap() {
        //Given
        var sepaRequest = createSepaInitiatePaymentRequest();
        var expectedResult = createUkInitiatePaymentRequest();

        //When
        var result = subject.map(sepaRequest);

        //Then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    private InitiateUkDomesticPaymentRequest createUkInitiatePaymentRequest() {
        var debtorAccount = new UkAccountDTO("GB91ABNA0417164322",
                AccountIdentifierScheme.IBAN,
                "Some Debtor Name",
                null);
        var creditorAccount = new UkAccountDTO("GB91ABNA0417164300",
                AccountIdentifierScheme.IBAN,
                "Some Creditor Name",
                null);
        var requestDTO = new InitiateUkDomesticPaymentRequestDTO(
                "B7F2761C",
                CurrencyCode.EUR.name(),
                new BigDecimal("5877.78"),
                creditorAccount,
                debtorAccount,
                "Remittance Unstructured",
                Collections.singletonMap(DynamicFieldNames.DEBTOR_NAME.getValue(), debtorAccount.getAccountHolderName()));

        return new InitiateUkDomesticPaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(new HashMap<>())
                .setSigner(signer)
                .setState("providerState")
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(null)
                .build();
    }

    private InitiatePaymentRequest createSepaInitiatePaymentRequest() {
        var dynamicFields = new DynamicFields();
        dynamicFields.setDebtorName("Some Debtor Name");
        var sepaRequestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "GB91ABNA0417164300"))
                .creditorName("Some Creditor Name")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "GB91ABNA0417164322"))
                .endToEndIdentification("B7F2761C")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("5877.78")))
                .remittanceInformationUnstructured("Remittance Unstructured")
                .dynamicFields(dynamicFields)
                .executionDate(LocalDate.now())
                .build();
        return new InitiatePaymentRequestBuilder()
                .setRequestDTO(sepaRequestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(new HashMap<>())
                .setSigner(signer)
                .setState("providerState")
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(null)
                .build();
    }
}