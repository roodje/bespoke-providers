package com.yolt.providers.stet.boursoramagroup.common.service.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.StetAuthenticationApproach;
import com.yolt.providers.stet.generic.dto.payment.StetCreditTransferTransactionDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPartyIdentificationDTO;
import com.yolt.providers.stet.generic.dto.payment.StetSupplementaryDataDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentHttpRequestBodyProvider;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public class BoursoramaGroupInitiatePaymentHttpRequestBodyProvider extends StetInitiatePaymentHttpRequestBodyProvider {

    public BoursoramaGroupInitiatePaymentHttpRequestBodyProvider(Clock clock, DateTimeSupplier dateTimeSupplier) {
        super(clock, dateTimeSupplier);
    }

    @Override
    protected StetCreditTransferTransactionDTO mapToCreditTransferTransaction(SepaInitiatePaymentRequestDTO request,
                                                                              OffsetDateTime currentDateTime) {
        return StetCreditTransferTransactionDTO.builder()
                .remittanceInformation(mapToRemittanceInformation(request))
                .instructedAmount(mapToInstructedAmount(request))
                .paymentId(mapToPaymentIdentification(request))
                .build();
    }

    @Override
    protected StetPartyIdentificationDTO mapToInitiatingParty(SepaInitiatePaymentRequestDTO requestDTO,
                                                              DefaultAuthenticationMeans authMeans) {
        return StetPartyIdentificationDTO.builder()
                .name(Objects.requireNonNull(requestDTO.getDynamicFields()).getDebtorName())
                .build();
    }

    protected StetSupplementaryDataDTO mapToSupplementaryData(StetInitiatePreExecutionResult preExecutionResult) {
        String redirectUrl = preExecutionResult.getBaseClientRedirectUrl();
        if (StringUtils.isEmpty(redirectUrl)) {
            throw new IllegalStateException("Unable to create PaymentRequestResource due to missing base client redirect URL");
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        return StetSupplementaryDataDTO.builder()
                .appliedAuthenticationApproach(StetAuthenticationApproach.REDIRECT)
                .successfulReportUrl(uriBuilder.queryParam("state", preExecutionResult.getState()).toUriString())
                .unsuccessfulReportUrl(uriBuilder.queryParam("error", "wrong").toUriString())
                .build();
    }
}
