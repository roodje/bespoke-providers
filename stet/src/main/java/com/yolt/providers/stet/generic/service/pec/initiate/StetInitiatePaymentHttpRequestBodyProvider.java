package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
public class StetInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<StetInitiatePreExecutionResult, StetPaymentInitiationRequestDTO> {

    protected final Clock clock;
    protected final DateTimeSupplier dateTimeSupplier;

    @Override
    public StetPaymentInitiationRequestDTO provideHttpRequestBody(StetInitiatePreExecutionResult preExecutionResult) {
        OffsetDateTime currentDateTime = OffsetDateTime.now(clock);
        SepaInitiatePaymentRequestDTO requestDTO = preExecutionResult.getSepaRequestDTO();

        StetCreditTransferTransaction creditTransferTransaction = mapToCreditTransferTransaction(requestDTO, currentDateTime);

        return StetPaymentInitiationRequestDTO.builder()
                .paymentInformationId(createUniqueId())
                .beneficiary(mapToBeneficiary(requestDTO))
                .numberOfTransactions(1)
                .initiatingParty(mapToInitiatingParty(requestDTO, preExecutionResult.getAuthMeans()))
                .paymentTypeInformation(createPaymentTypeInformation())
                .creationDateTime(mapToCreationDateTime(requestDTO, currentDateTime))
                .debtorAccount(mapToAccountIdentification(requestDTO.getDebtorAccount()))
                .chargeBearer(StetChargeBearer.SLEV)
                .supplementaryData(mapToSupplementaryData(preExecutionResult))
                .requestedExecutionDate(mapToRequestedExecutionDate(requestDTO, currentDateTime))
                .creditTransferTransaction(Collections.singletonList(creditTransferTransaction))
                .build();
    }

    protected OffsetDateTime mapToRequestedExecutionDate(SepaInitiatePaymentRequestDTO requestDTO, OffsetDateTime currentDateTime) {
        return dateTimeSupplier.convertOrGetDefaultOffsetDateTime(requestDTO.getExecutionDate(), currentDateTime.plusSeconds(30));
    }

    protected StetRemittanceInformationDTO mapToRemittanceInformation(SepaInitiatePaymentRequestDTO requestDTO) {
        return StetRemittanceInformationDTO.builder()
                .unstructured(Collections.singletonList(requestDTO.getRemittanceInformationUnstructured()))
                .build();
    }

    protected StetAmountTypeDTO mapToInstructedAmount(SepaInitiatePaymentRequestDTO request) {
        return StetAmountTypeDTO.builder()
                .amount(request.getInstructedAmount().getAmount().floatValue())
                .currency(CurrencyCode.EUR.name())
                .build();
    }

    protected StetPaymentIdentificationDTO mapToPaymentIdentification(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentIdentificationDTO.builder()
                .instructionId(createUniqueId())
                .endToEndId(request.getEndToEndIdentification())
                .build();
    }

    protected StetPaymentBeneficiaryDTO mapToBeneficiary(SepaInitiatePaymentRequestDTO request) {
        return StetPaymentBeneficiaryDTO.builder()
                .creditor(mapToPartyIdentification(request.getCreditorName()))
                .creditorAccount(mapToAccountIdentification(request.getCreditorAccount()))
                .build();
    }

    protected StetCreditTransferTransaction mapToCreditTransferTransaction(SepaInitiatePaymentRequestDTO request,
                                                                           OffsetDateTime currentDateTime) {
        return StetCreditTransferTransactionDTO.builder()
                .requestedExecutionDate(mapToRequestedExecutionDate(request, currentDateTime))
                .remittanceInformation(mapToRemittanceInformation(request))
                .instructedAmount(mapToInstructedAmount(request))
                .paymentId(mapToPaymentIdentification(request))
                .beneficiary(mapToBeneficiary(request))
                .build();
    }

    protected String createUniqueId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    protected StetPartyIdentificationDTO mapToInitiatingParty(SepaInitiatePaymentRequestDTO requestDTO,
                                                              DefaultAuthenticationMeans authMeans) { //NOSONAR It allows others to use it
        return mapToPartyIdentification(requestDTO.getCreditorName());
    }

    protected StetPartyIdentificationDTO mapToPartyIdentification(String name) {
        return StetPartyIdentificationDTO.builder()
                .name(name)
                .build();
    }

    protected StetPaymentTypeInformationDTO createPaymentTypeInformation() {
        return StetPaymentTypeInformationDTO.builder()
                .serviceLevel(StetServiceLevel.SEPA)
                .categoryPurpose(StetCategoryPurpose.CASH)
                .build();
    }

    protected OffsetDateTime mapToCreationDateTime(SepaInitiatePaymentRequestDTO requestDTO, //NOSONAR It allows others to use it
                                                   OffsetDateTime currentDateTime) {
        return currentDateTime;
    }

    protected StetSupplementaryData mapToSupplementaryData(StetInitiatePreExecutionResult preExecutionResult) {
        String redirectUrl = preExecutionResult.getBaseClientRedirectUrl();
        if (StringUtils.isEmpty(redirectUrl)) {
            throw new IllegalStateException("Unable to create PaymentRequestResource due to missing base client redirect URL");
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        return StetSupplementaryDataDTO.builder()
                .appliedAuthenticationApproach(StetAuthenticationApproach.REDIRECT)
                .successfulReportUrl(URLEncoder.encode(uriBuilder.queryParam("state", preExecutionResult.getState()).toUriString(), UTF_8))
                .unsuccessfulReportUrl(URLEncoder.encode(uriBuilder.queryParam("error", "wrong").toUriString(), UTF_8))
                .build();
    }

    protected StetAccountIdentificationDTO mapToAccountIdentification(SepaAccountDTO accountDTO) {
        if (!ObjectUtils.isEmpty(accountDTO)) {
            CurrencyCode currency = accountDTO.getCurrency();
            return StetAccountIdentificationDTO.builder()
                    .iban(accountDTO.getIban())
                    .currency(Objects.nonNull(currency) ? currency.name() : null)
                    .build();
        }
        return null;
    }
}
