package com.yolt.providers.stet.bpcegroup.common.service.pec;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentHttpRequestBodyProvider;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.util.Collections;
import java.util.List;

public class BpceGroupInitiatePaymentHttpRequestBodyProvider extends StetInitiatePaymentHttpRequestBodyProvider {


    public BpceGroupInitiatePaymentHttpRequestBodyProvider(Clock clock, DateTimeSupplier dateTimeSupplier) {
        super(clock, dateTimeSupplier);
    }

    @Override
    public StetPaymentInitiationRequestDTO provideHttpRequestBody(StetInitiatePreExecutionResult preExecutionResult) {
        SepaInitiatePaymentRequestDTO requestDTO = preExecutionResult.getSepaRequestDTO();
        OffsetDateTime currentDateTime = OffsetDateTime.now(clock);
        StetCreditTransferTransaction creditTransferTransaction = mapToCreditTransferTransaction(requestDTO, currentDateTime);

        return StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(createUniqueId())
                .creationDateTime(currentDateTime)
                .numberOfTransactions(1)
                .initiatingParty(mapToInitiatingParty(requestDTO, preExecutionResult.getAuthMeans()))
                .paymentTypeInformation(createPaymentTypeInformation())
                .debtorAccount(mapToAccountIdentification(requestDTO.getDebtorAccount()))
                .beneficiary(mapToBeneficiary(requestDTO))
                .chargeBearer(StetChargeBearer.SLEV)
                .requestedExecutionDate(executionToOffsetDateTimeOrDefault(requestDTO.getExecutionDate()))
                .creditTransferTransaction(Collections.singletonList(creditTransferTransaction))
                .supplementaryData(mapToSupplementaryDataArray(preExecutionResult))
                .build();
    }

    @Override
    protected StetAccountIdentificationDTO mapToAccountIdentification(SepaAccountDTO accountDTO) {
        if (!ObjectUtils.isEmpty(accountDTO)) {
            return StetAccountIdentificationDTO.builder()
                    .iban(accountDTO.getIban())
                    .build();
        }
        return null;
    }

    @Override
    protected StetCreditTransferTransaction mapToCreditTransferTransaction(SepaInitiatePaymentRequestDTO request,
                                                                           OffsetDateTime currentDateTime) {
        return StetCreditTransferTransactionDTO.builder()
                .remittanceInformation(mapToRemittanceInformation(request))
                .instructedAmount(mapToInstructedAmount(request))
                .paymentId(mapToPaymentIdentification(request))
                .build();
    }

    @Override
    protected StetAmountTypeDTO mapToInstructedAmount(SepaInitiatePaymentRequestDTO request) {
        return StetAmountTypeDTO.builder()
                .amount(request.getInstructedAmount().getAmount().floatValue())
                .currency(CurrencyCode.EUR.name())
                .build();
    }

    protected StetSupplementaryDataArrayDTO mapToSupplementaryDataArray(StetInitiatePreExecutionResult preExecutionResult) {
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

    protected OffsetDateTime executionToOffsetDateTimeOrDefault(LocalDate executionDate) {
        LocalDateTime executionWithTime = LocalDateTime.of(LocalDate.now(clock), LocalTime.of(8, 0));
        if (executionDate != null) {
            executionWithTime = executionDate.atTime(8, 0, 0);
        }
        ZoneOffset parisOffset = ZoneId.of("Europe/Paris").getRules().getOffset(executionWithTime);
        return OffsetDateTime.of(executionWithTime, parisOffset);
    }
}
