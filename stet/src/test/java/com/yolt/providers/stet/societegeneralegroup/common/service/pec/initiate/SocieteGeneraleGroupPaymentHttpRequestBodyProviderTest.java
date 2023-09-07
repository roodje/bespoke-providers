package com.yolt.providers.stet.societegeneralegroup.common.service.pec.initiate;

import com.yolt.providers.common.pis.sepa.InstructionPriority;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.SocieteGeneraleDateTimeSupplier;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SocieteGeneraleGroupPaymentHttpRequestBodyProviderTest {

    private Clock clock = Clock.systemUTC();
    private SocieteGeneraleDateTimeSupplier dateTimeSupplier = new SocieteGeneraleDateTimeSupplier(clock);
    private SocieteGeneraleGroupPaymentHttpRequestBodyProvider requestBodyProvider =
            new SocieteGeneraleGroupPaymentHttpRequestBodyProvider(dateTimeSupplier);

    @Test
    void shouldReturnProperRequestBodyDtoWithCurrentDateTimeAsExecutionDateTimeWhenExecutionDateTimeIsNotProvidedInRequest() {
        //given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("FR7630006000011234567890189")
                        .build())
                .creditorName("Buzz Lightyear")
                .endToEndIdentification("4ccb2450-eeb1-11ea-3333-0242ac120002")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal(124.66))
                        .build())
                .instructionPriority(InstructionPriority.NORMAL)
                .remittanceInformationUnstructured("To infinity and beyond!")
                .build();
        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .sepaRequestDTO(requestDTO)
                .baseClientRedirectUrl("https://yolt.callback.io")
                .state("1234-5678")
                .build();

        StetPaymentInitiationRequestDTO expectedDto = StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(requestDTO.getEndToEndIdentification())
                .numberOfTransactions(1)
                .initiatingParty(StetPartyIdentificationDTO.builder()
                        .name("Buzz Lightyear").build())
                .paymentTypeInformation(StetPaymentTypeInformationDTO.builder()
                        .serviceLevel(StetServiceLevel.SEPA).build())
                .beneficiary(StetPaymentBeneficiaryDTO.builder()
                        .creditor(StetPartyIdentificationDTO.builder()
                                .name("Buzz Lightyear").build())
                        .creditorAccount(StetAccountIdentificationDTO.builder()
                                .iban("FR7630006000011234567890189").build())
                        .build())
                .creditTransferTransaction(List.of(StetCreditTransferTransactionDTO.builder()
                        .paymentId(StetPaymentIdentificationDTO.builder()
                                .instructionId("4ccb2450-eeb1-11ea-3333-0242ac120002")
                                .endToEndId("4ccb2450-eeb1-11ea-3333-0242ac120002").build())
                        .instructedAmount(StetAmountTypeDTO.builder()
                                .amount(124.66f)
                                .currency("EUR").build())
                        .remittanceInformation(StetRemittanceInformationDTO.builder()
                                .unstructured(List.of("To infinity and beyond!")).build())
                        .build()))
                .supplementaryData(StetSupplementaryDataArrayDTO.builder()
                        .acceptedAuthenticationApproach(List.of(StetAuthenticationApproach.REDIRECT))
                        .successfulReportUrl("https://yolt.callback.io?state=1234-5678")
                        .unsuccessfulReportUrl("https://yolt.callback.io?state=1234-5678&error=wrong")
                        .build())
                .build();
        OffsetDateTime currentDateTime = Instant.now(clock).atOffset(ZoneOffset.UTC);

        //when
        StetPaymentInitiationRequestDTO result = requestBodyProvider.provideHttpRequestBody(preExecutionResult);

        //then
        assertThat(result).usingRecursiveComparison().ignoringFields("creationDateTime", "requestedExecutionDate").isEqualTo(expectedDto);
        assertThat(result.getRequestedExecutionDate()).isAfter(currentDateTime).isBefore(currentDateTime.plusSeconds(600));
    }

    @Test
    void shouldReturnProperRequestBodyDtoWithExecutionDateTimeProvidedInRequest() {
        //given
        LocalDate executionDate = LocalDate.now().plusDays(20);
        OffsetDateTime expectedExecutionDateTime = OffsetDateTime.of(executionDate, LocalTime.of(8, 0), ZoneId.of("Europe/Paris").getRules().getOffset(LocalDateTime.now())).withOffsetSameInstant(ZoneOffset.UTC);
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("FR7630006000011234567890189")
                        .build())
                .creditorName("Buzz Lightyear")
                .endToEndIdentification("4ccb2450-eeb1-11ea-3333-0242ac120002")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("124.66"))
                        .build())
                .instructionPriority(InstructionPriority.NORMAL)
                .remittanceInformationUnstructured("To infinity and beyond!")
                .executionDate(executionDate)
                .build();
        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .sepaRequestDTO(requestDTO)
                .baseClientRedirectUrl("https://yolt.callback.io")
                .state("1234-5678")
                .build();

        StetPaymentInitiationRequestDTO expectedDto = StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(requestDTO.getEndToEndIdentification())
                .requestedExecutionDate(expectedExecutionDateTime)
                .numberOfTransactions(1)
                .initiatingParty(StetPartyIdentificationDTO.builder()
                        .name("Buzz Lightyear").build())
                .paymentTypeInformation(StetPaymentTypeInformationDTO.builder()
                        .serviceLevel(StetServiceLevel.SEPA).build())
                .beneficiary(StetPaymentBeneficiaryDTO.builder()
                        .creditor(StetPartyIdentificationDTO.builder()
                                .name("Buzz Lightyear").build())
                        .creditorAccount(StetAccountIdentificationDTO.builder()
                                .iban("FR7630006000011234567890189").build())
                        .build())
                .creditTransferTransaction(List.of(StetCreditTransferTransactionDTO.builder()
                        .paymentId(StetPaymentIdentificationDTO.builder()
                                .instructionId("4ccb2450-eeb1-11ea-3333-0242ac120002")
                                .endToEndId("4ccb2450-eeb1-11ea-3333-0242ac120002").build())
                        .instructedAmount(StetAmountTypeDTO.builder()
                                .amount(124.66f)
                                .currency("EUR").build())
                        .remittanceInformation(StetRemittanceInformationDTO.builder()
                                .unstructured(List.of("To infinity and beyond!")).build())
                        .build()))
                .supplementaryData(StetSupplementaryDataArrayDTO.builder()
                        .acceptedAuthenticationApproach(List.of(StetAuthenticationApproach.REDIRECT))
                        .successfulReportUrl("https://yolt.callback.io?state=1234-5678")
                        .unsuccessfulReportUrl("https://yolt.callback.io?state=1234-5678&error=wrong")
                        .build())
                .build();

        //when
        StetPaymentInitiationRequestDTO result = requestBodyProvider.provideHttpRequestBody(preExecutionResult);

        //then
        assertThat(result).usingRecursiveComparison().ignoringFields("creationDateTime").isEqualTo(expectedDto);
    }

    @Test
    void shouldReturnProperRequestBodyDtoWithDebtorAccountWhenDebtorAccountIsProvidedInRequest() {
        //given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("FR7630006000011234567890189")
                        .build())
                .debtorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("FR7630006000011234567890123")
                        .build())
                .creditorName("Buzz Lightyear")
                .endToEndIdentification("4ccb2450-eeb1-11ea-3333-0242ac120002")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal(124.66))
                        .build())
                .instructionPriority(InstructionPriority.NORMAL)
                .remittanceInformationUnstructured("To infinity and beyond!")
                .build();
        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .sepaRequestDTO(requestDTO)
                .baseClientRedirectUrl("https://yolt.callback.io")
                .state("1234-5678")
                .build();

        StetPaymentInitiationRequestDTO expectedDto = StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(requestDTO.getEndToEndIdentification())
                .numberOfTransactions(1)
                .debtorAccount(StetAccountIdentificationDTO.builder()
                        .iban("FR7630006000011234567890123").build())
                .initiatingParty(StetPartyIdentificationDTO.builder()
                        .name("Buzz Lightyear").build())
                .paymentTypeInformation(StetPaymentTypeInformationDTO.builder()
                        .serviceLevel(StetServiceLevel.SEPA).build())
                .beneficiary(StetPaymentBeneficiaryDTO.builder()
                        .creditor(StetPartyIdentificationDTO.builder()
                                .name("Buzz Lightyear").build())
                        .creditorAccount(StetAccountIdentificationDTO.builder()
                                .iban("FR7630006000011234567890189").build())
                        .build())
                .creditTransferTransaction(List.of(StetCreditTransferTransactionDTO.builder()
                        .paymentId(StetPaymentIdentificationDTO.builder()
                                .instructionId("4ccb2450-eeb1-11ea-3333-0242ac120002")
                                .endToEndId("4ccb2450-eeb1-11ea-3333-0242ac120002").build())
                        .instructedAmount(StetAmountTypeDTO.builder()
                                .amount(124.66f)
                                .currency("EUR").build())
                        .remittanceInformation(StetRemittanceInformationDTO.builder()
                                .unstructured(List.of("To infinity and beyond!")).build())
                        .build()))
                .supplementaryData(StetSupplementaryDataArrayDTO.builder()
                        .acceptedAuthenticationApproach(List.of(StetAuthenticationApproach.REDIRECT))
                        .successfulReportUrl("https://yolt.callback.io?state=1234-5678")
                        .unsuccessfulReportUrl("https://yolt.callback.io?state=1234-5678&error=wrong")
                        .build())
                .build();
        OffsetDateTime currentDateTime = Instant.now(clock).atOffset(ZoneOffset.UTC);

        //when
        StetPaymentInitiationRequestDTO result = requestBodyProvider.provideHttpRequestBody(preExecutionResult);

        //then
        assertThat(result).usingRecursiveComparison().ignoringFields("creationDateTime", "requestedExecutionDate").isEqualTo(expectedDto);
        assertThat(result.getRequestedExecutionDate()).isAfter(currentDateTime).isBefore(currentDateTime.plusSeconds(600));
    }
}