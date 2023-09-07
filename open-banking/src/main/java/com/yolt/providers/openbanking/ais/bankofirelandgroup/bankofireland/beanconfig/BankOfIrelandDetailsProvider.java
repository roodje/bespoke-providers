package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.beanconfig;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.GB;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class BankOfIrelandDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    public static final String BANK_OF_IRELAND_SITE_ID = "ac75d67d-5ede-4972-94a8-3b8481fa2145";
    public static final String BANK_OF_IRELAND_PROVIDER_KEY = "BANK_OF_IRELAND";
    public static final String BANK_OF_IRELAND_PROVIDER_NAME = "Bank of Ireland";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(BANK_OF_IRELAND_SITE_ID, BANK_OF_IRELAND_PROVIDER_NAME, BANK_OF_IRELAND_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT), of(GB))
                    .groupingBy(BANK_OF_IRELAND_PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT), PIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(BANK_OF_IRELAND_SITE_ID))
                    .providerKey(BANK_OF_IRELAND_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Map.of(DynamicFieldNames.REMITTANCE_INFORMATION_STRUCTURED, new DynamicFieldOptions(false)))
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.UKDOMESTIC)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS;
    }
}
