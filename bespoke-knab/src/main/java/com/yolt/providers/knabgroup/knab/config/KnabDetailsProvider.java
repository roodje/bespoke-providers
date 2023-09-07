package com.yolt.providers.knabgroup.knab.config;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import com.yolt.providers.common.providerdetail.dto.PaymentMethod;
import com.yolt.providers.common.providerdetail.dto.PisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.NL;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class KnabDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    public static final String KNAB_NL_SITE_ID = "f3eba1bc-05d6-4e6f-98b4-610e70a81046";
    private static final String PROVIDER_KEY = "KNAB_BANK_NL";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(KNAB_NL_SITE_ID, "Knab Bank (NL)", PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, SAVINGS_ACCOUNT), of(NL))
                    .groupingBy("Knab Bank")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT), PIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(KNAB_NL_SITE_ID))
                    .providerKey(PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(false)
                    .paymentMethod(PaymentMethod.SEPA)
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
