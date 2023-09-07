package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.common.providerdetail.dto.DynamicFieldNames;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class SepaInitiateSheduledPaymentRequestMapperTest {

    private SepaInitiateSheduledPaymentRequestMapper subject = new SepaInitiateSheduledPaymentRequestMapper(new SepaDynamicFieldsMapper());

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private AuthenticationMeansReference authenticationMeansReference;

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

    private InitiateUkDomesticScheduledPaymentRequest createUkInitiatePaymentRequest() {
        var debtorAccount = new UkAccountDTO("GB91ABNA0417164322",
                AccountIdentifierScheme.IBAN,
                "Some Debtor Name",
                null);
        var creditorAccount = new UkAccountDTO("GB91ABNA0417164300",
                AccountIdentifierScheme.IBAN,
                "Some Creditor Name",
                null);
        var requestDTO = new InitiateUkDomesticScheduledPaymentRequestDTO(
                "B7F2761C",
                CurrencyCode.EUR.name(),
                new BigDecimal("5877.78"),
                creditorAccount,
                debtorAccount,
                "Remittance Unstructured",
                Collections.singletonMap(DynamicFieldNames.DEBTOR_NAME.getValue(), debtorAccount.getAccountHolderName()),
                OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC));

        return new InitiateUkDomesticScheduledPaymentRequest(
                requestDTO,
                "https://www.yolt.com/callback/payment",
                "providerState",
                new HashMap<>(),
                signer,
                restTemplateManager,
                "111.111.111.111",
                authenticationMeansReference
        );

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
                .setPsuIpAddress("111.111.111.111")
                .setAuthenticationMeansReference(authenticationMeansReference)
                .build();
    }
}