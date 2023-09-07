package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RequiredArgsConstructor
public class SepaInitiateSheduledPaymentRequestMapper {

    private final SepaDynamicFieldsMapper dynamicFieldsMapper;

    public InitiateUkDomesticScheduledPaymentRequest map(InitiatePaymentRequest sepaRequest) {
        return new InitiateUkDomesticScheduledPaymentRequest(
                mapRequestDTO(sepaRequest.getRequestDTO()),
                sepaRequest.getBaseClientRedirectUrl(),
                sepaRequest.getState(),
                sepaRequest.getAuthenticationMeans(),
                sepaRequest.getSigner(),
                sepaRequest.getRestTemplateManager(),
                sepaRequest.getPsuIpAddress(),
                sepaRequest.getAuthenticationMeansReference()
        );
    }

    private InitiateUkDomesticScheduledPaymentRequestDTO mapRequestDTO(SepaInitiatePaymentRequestDTO requestDTO) {
        String debtorName = requestDTO.getDynamicFields() == null ? null : requestDTO.getDynamicFields().getDebtorName();
        OffsetDateTime executionDate = requestDTO.getExecutionDate() == null ? null : OffsetDateTime.of(requestDTO.getExecutionDate(), LocalTime.MIN, ZoneOffset.UTC);
        return new InitiateUkDomesticScheduledPaymentRequestDTO(
                requestDTO.getEndToEndIdentification(),
                requestDTO.getCreditorAccount().getCurrency().name(),
                requestDTO.getInstructedAmount().getAmount(),
                mapAccountDTO(requestDTO.getCreditorAccount(), requestDTO.getCreditorName()),
                mapAccountDTO(requestDTO.getDebtorAccount(), debtorName),
                requestDTO.getRemittanceInformationUnstructured(),
                dynamicFieldsMapper.map(requestDTO.getDynamicFields()),
                executionDate
        );
    }

    private UkAccountDTO mapAccountDTO(SepaAccountDTO sepaAccount, String name) {
        if (sepaAccount == null) {
            return null;
        }

        return new UkAccountDTO(
                sepaAccount.getIban(),
                AccountIdentifierScheme.IBAN,
                name,
                null
        );
    }
}
