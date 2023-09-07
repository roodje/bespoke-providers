package com.yolt.providers.openbanking.ais.hsbcgroup.hsbc.beanconfig;

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
public class HsbcDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    public static final String HSBC_SITE_ID = "8b21aab6-e0a3-43ae-be5e-def71509bef0";
    private static final String HSBC_PROVIDER_KEY = "HSBC";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(HSBC_SITE_ID, "HSBC Bank", HSBC_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, SAVINGS_ACCOUNT, CREDIT_CARD), of(GB))
                    .groupingBy("HSBC Bank")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT), PIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(HSBC_SITE_ID))
                    .providerKey(HSBC_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Map.of(DynamicFieldNames.REMITTANCE_INFORMATION_STRUCTURED, new DynamicFieldOptions(true)))
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.UKDOMESTIC)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
            ,
            PisSiteDetails.builder()
                    .id(UUID.fromString(HSBC_SITE_ID))
                    .providerKey(HSBC_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SCHEDULED)
                    .dynamicFields(Map.of(DynamicFieldNames.REMITTANCE_INFORMATION_STRUCTURED, new DynamicFieldOptions(true)))
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
