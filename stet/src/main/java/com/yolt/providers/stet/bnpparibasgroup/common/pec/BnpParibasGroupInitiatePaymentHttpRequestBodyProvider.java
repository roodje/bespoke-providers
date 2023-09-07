package com.yolt.providers.stet.bnpparibasgroup.common.pec;

import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentHttpRequestBodyProvider;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BnpParibasGroupInitiatePaymentHttpRequestBodyProvider extends StetInitiatePaymentHttpRequestBodyProvider {

    public BnpParibasGroupInitiatePaymentHttpRequestBodyProvider(Clock clock, DateTimeSupplier dateTimeSupplier) {
        super(clock, dateTimeSupplier);
    }

    @Override
    protected OffsetDateTime mapToCreationDateTime(SepaInitiatePaymentRequestDTO requestDTO, OffsetDateTime currentDateTime) {
        return super.mapToCreationDateTime(requestDTO, currentDateTime)
                .truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    protected StetPartyIdentificationDTO mapToInitiatingParty(SepaInitiatePaymentRequestDTO requestDTO, DefaultAuthenticationMeans authMeans) {
        return StetPartyIdentificationDTO.builder()
                .name("Yolt")
                .build();
    }

    @Override
    protected StetCreditTransferTransaction mapToCreditTransferTransaction(SepaInitiatePaymentRequestDTO request, OffsetDateTime currentDateTime) {
        return StetCreditTransferTransactionNoUnstructured.builder()
                .instructedAmount(mapToInstructedAmount(request))
                .paymentId(mapToPaymentIdentification(request))
                .remittanceInformation(List.of(request.getRemittanceInformationUnstructured()))
                .build();
    }

    @Override
    protected StetSupplementaryDataArrayDTO mapToSupplementaryData(StetInitiatePreExecutionResult preExecutionResult) {
        String baseClientRedirectUrl = preExecutionResult.getBaseClientRedirectUrl();
        if (StringUtils.isEmpty(baseClientRedirectUrl)) {
            throw new IllegalStateException("Unable to create PaymentRequestResource due to missing base client redirect URL");
        }
        String redirectUrl = UriComponentsBuilder.fromUriString(baseClientRedirectUrl)
                .queryParam("state", preExecutionResult.getState())
                .toUriString();
        return StetSupplementaryDataArrayDTO.builder()
                .acceptedAuthenticationApproach(List.of(StetAuthenticationApproach.REDIRECT))
                .successfulReportUrl(redirectUrl)
                .unsuccessfulReportUrl(redirectUrl)
                .build();
    }
}
