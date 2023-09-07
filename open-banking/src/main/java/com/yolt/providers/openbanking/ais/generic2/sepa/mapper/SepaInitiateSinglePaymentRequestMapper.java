package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SepaInitiateSinglePaymentRequestMapper {

    private final SepaDynamicFieldsMapper dynamicFieldsMapper;

    public InitiateUkDomesticPaymentRequest map(InitiatePaymentRequest sepaRequest) {
        return new InitiateUkDomesticPaymentRequest(
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

    private InitiateUkDomesticPaymentRequestDTO mapRequestDTO(SepaInitiatePaymentRequestDTO requestDTO) {
        String debtorName = requestDTO.getDynamicFields() == null ? null : requestDTO.getDynamicFields().getDebtorName();
        return new InitiateUkDomesticPaymentRequestDTO(
                requestDTO.getEndToEndIdentification(),
                requestDTO.getCreditorAccount().getCurrency().name(),
                requestDTO.getInstructedAmount().getAmount(),
                mapAccountDTO(requestDTO.getCreditorAccount(), requestDTO.getCreditorName()),
                mapAccountDTO(requestDTO.getDebtorAccount(), debtorName),
                requestDTO.getRemittanceInformationUnstructured(),
                dynamicFieldsMapper.map(requestDTO.getDynamicFields())
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
