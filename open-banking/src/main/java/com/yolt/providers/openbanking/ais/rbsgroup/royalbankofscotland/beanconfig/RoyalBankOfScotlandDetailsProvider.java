package com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotland.beanconfig;

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
public class RoyalBankOfScotlandDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    private static final String ROYAL_BANK_OF_SCOTLAND_SITE_ID = "5fe1e9f8-eb5f-4812-a6a6-2002759db545";
    private static final String PROVIDER_NAME = "Royal Bank of Scotland";
    private static final String PROVIDER_KEY = "ROYAL_BANK_OF_SCOTLAND";

    private static final List<PisSiteDetails> PIS_SITE_DETAILS_LIST = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(ROYAL_BANK_OF_SCOTLAND_SITE_ID))
                    .providerKey(PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Map.of(DynamicFieldNames.REMITTANCE_INFORMATION_STRUCTURED, new DynamicFieldOptions(false)))
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.UKDOMESTIC)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(ROYAL_BANK_OF_SCOTLAND_SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT), of(GB))
                    .groupingBy("RBS")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT), PIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS_LIST;
    }
}
