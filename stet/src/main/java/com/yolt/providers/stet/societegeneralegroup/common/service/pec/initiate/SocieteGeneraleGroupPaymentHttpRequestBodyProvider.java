package com.yolt.providers.stet.societegeneralegroup.common.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.SocieteGeneraleDateTimeSupplier;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class SocieteGeneraleGroupPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<StetInitiatePreExecutionResult, StetPaymentInitiationRequestDTO> {

    public static final int FIVE_MINUTES_IN_SECONDS = 300;
    private final SocieteGeneraleDateTimeSupplier dateTimeSupplier;

    @Override
    public StetPaymentInitiationRequestDTO provideHttpRequestBody(StetInitiatePreExecutionResult preExecutionResult) {
        SepaInitiatePaymentRequestDTO requestDTO = preExecutionResult.getSepaRequestDTO();
        OffsetDateTime currentDateTime = dateTimeSupplier.getDefaultOffsetDateTime();

        return StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(requestDTO.getEndToEndIdentification())
                .creationDateTime(currentDateTime)
                .numberOfTransactions(1)
                .initiatingParty(mapToInitiatingParty(Objects.requireNonNull(requestDTO.getCreditorName())))
                .debtorAccount(mapToAccountIdentification(requestDTO.getDebtorAccount()))
                .paymentTypeInformation(createPaymentTypeInformation())
                .beneficiary(mapToBeneficiary(requestDTO))
                .creditTransferTransaction(List.of(mapToCreditTransferTransaction(requestDTO)))
                .requestedExecutionDate(dateTimeSupplier.convertOrGetDefaultOffsetDateTime(requestDTO.getExecutionDate(), currentDateTime.plusSeconds(FIVE_MINUTES_IN_SECONDS)))
                .supplementaryData(mapToSupplementaryData(preExecutionResult))
                .build();
    }

    private StetPartyIdentificationDTO mapToInitiatingParty(String debtorName) {
        return StetPartyIdentificationDTO.builder()
                .name(debtorName)
                .build();
    }

    private StetPaymentTypeInformationDTO createPaymentTypeInformation() {
        return StetPaymentTypeInformationDTO.builder()
                .serviceLevel(StetServiceLevel.SEPA)
                .build();
    }

    private StetPaymentBeneficiaryDTO mapToBeneficiary(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentBeneficiaryDTO.builder()
                .creditor(mapToPartyIdentification(Objects.requireNonNull(request.getCreditorName())))
                .creditorAccount(mapToAccountIdentification(Objects.requireNonNull(request.getCreditorAccount())))
                .build();
    }

    private StetPartyIdentificationDTO mapToPartyIdentification(String name) {
        return StetPartyIdentificationDTO.builder()
                .name(name)
                .build();
    }

    private StetAccountIdentificationDTO mapToAccountIdentification(SepaAccountDTO accountDTO) {
        if (!ObjectUtils.isEmpty(accountDTO)) {
            return StetAccountIdentificationDTO.builder()
                    .iban(accountDTO.getIban())
                    .build();
        }
        return null;
    }

    private StetCreditTransferTransactionDTO mapToCreditTransferTransaction(SepaInitiatePaymentRequestDTO request) {
        return StetCreditTransferTransactionDTO.builder()
                .remittanceInformation(mapToRemittanceInformation(request))
                .instructedAmount(mapToInstructedAmount(request))
                .paymentId(mapToPaymentIdentification(request))
                .build();
    }

    private StetRemittanceInformationDTO mapToRemittanceInformation(SepaInitiatePaymentRequestDTO requestDTO) {
        return StetRemittanceInformationDTO.builder()
                .unstructured(Collections.singletonList(requestDTO.getRemittanceInformationUnstructured()))
                .build();
    }

    private StetAmountTypeDTO mapToInstructedAmount(SepaInitiatePaymentRequestDTO request) {
        return StetAmountTypeDTO.builder()
                .amount(request.getInstructedAmount().getAmount().floatValue())
                .currency(CurrencyCode.EUR.name())
                .build();
    }

    private StetPaymentIdentificationDTO mapToPaymentIdentification(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentIdentificationDTO.builder()
                .instructionId(request.getEndToEndIdentification())
                .endToEndId(request.getEndToEndIdentification())
                .build();
    }

    private StetSupplementaryDataArrayDTO mapToSupplementaryData(StetInitiatePreExecutionResult preExecutionResult) {
        String redirectUrl = preExecutionResult.getBaseClientRedirectUrl();
        if (StringUtils.isEmpty(redirectUrl)) {
            throw new IllegalStateException("Unable to create PaymentRequestResource due to missing base client redirect URL");
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        return StetSupplementaryDataArrayDTO.builder()
                .acceptedAuthenticationApproach(List.of(StetAuthenticationApproach.REDIRECT))
                .successfulReportUrl(uriBuilder.queryParam("state", preExecutionResult.getState()).toUriString())
                .unsuccessfulReportUrl(uriBuilder.queryParam("error", "wrong").toUriString())
                .build();
    }
}
