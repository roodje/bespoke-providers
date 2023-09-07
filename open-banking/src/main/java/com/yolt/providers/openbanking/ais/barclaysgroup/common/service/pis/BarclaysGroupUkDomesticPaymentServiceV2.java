package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.pis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.UkPaymentMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.pis.paymentservice.DefaultUkDomesticPaymentService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationDebtorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import org.springframework.util.StringUtils;

public class BarclaysGroupUkDomesticPaymentServiceV2 extends DefaultUkDomesticPaymentService {

    private static final String INCORRECT_SCHEMA_NAME = "SORTCODEACCOUNTNUMBER";
    private static final String CORRECT_SCHEMA_NAME = "SortCodeAccountNumber";
    private static final int END_TO_END_IDENTIFICATION_MAX_LENGTH = 31;
    private static final int NAME_MAX_LENGHT = 18;
    private static final String EXCEPTION_MESSAGE_PATTERN = "%s is too long, maximum allowed for Barclays is %d characters.";

    private final UkPaymentMapper paymentMapper;

    public BarclaysGroupUkDomesticPaymentServiceV2(final AuthenticationService authenticationService,
                                                   final RestClient restClient,
                                                   final ObjectMapper objectMapper,
                                                   final UkPaymentMapper paymentMapper,
                                                   final String endpointsVersion) {
        super(authenticationService, restClient, objectMapper, paymentMapper, endpointsVersion);
        this.paymentMapper = paymentMapper;
    }

    @Override
    protected OBWriteDomesticConsent4 getCreatePaymentRequestBody(final InitiateUkDomesticPaymentRequest request) {
        validatePaymentRequest(request.getRequestDTO());
        OBWriteDomesticConsent4 mappedRequest = paymentMapper.mapToSetupRequest(request);
        //Workaround for Barclays that doesn't want scheme name in uppercase.
        OBWriteDomestic2DataInitiation initiation = mappedRequest.getData().getInitiation();
        OBWriteDomestic2DataInitiationCreditorAccount creditorAccount = initiation.getCreditorAccount();
        OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = initiation.getDebtorAccount();
        String correctCreditorSchemaName = creditorAccount.getSchemeName().replace(INCORRECT_SCHEMA_NAME, CORRECT_SCHEMA_NAME);
        String correctDebtorSchemaName = debtorAccount.getSchemeName().replace(INCORRECT_SCHEMA_NAME, CORRECT_SCHEMA_NAME);
        creditorAccount.setSchemeName(correctCreditorSchemaName);
        debtorAccount.setSchemeName(correctDebtorSchemaName);
        return mappedRequest;
    }

    private void validatePaymentRequest(final InitiateUkDomesticPaymentRequestDTO requestDTO) {
        UkAccountDTO creditorAccount = requestDTO.getCreditorAccount();
        UkAccountDTO debtorAccount = requestDTO.getDebtorAccount();
        if (AccountIdentifierScheme.IBAN == debtorAccount.getAccountIdentifierScheme()
                || AccountIdentifierScheme.IBAN == creditorAccount.getAccountIdentifierScheme()) {
            throw new IllegalArgumentException("IBAN payments are not supported by Barclays.");
        }
        if (StringUtils.isEmpty(creditorAccount.getAccountHolderName()) || StringUtils.isEmpty(debtorAccount.getAccountHolderName())) {
            throw new IllegalArgumentException("Creditor and debtor name must be present.");
        }
        if (isLongerThanMax(creditorAccount) || isLongerThanMax(debtorAccount)) {
            throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE_PATTERN, "Creditor or debtor name", NAME_MAX_LENGHT));
        }
        if (requestDTO.getEndToEndIdentification().length() > END_TO_END_IDENTIFICATION_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE_PATTERN, "EndToEndIdentification",
                    END_TO_END_IDENTIFICATION_MAX_LENGTH));
        }
    }

    private boolean isLongerThanMax(final UkAccountDTO ukAccountDTO) {
        return ukAccountDTO.getAccountHolderName().length() > NAME_MAX_LENGHT;
    }
}
