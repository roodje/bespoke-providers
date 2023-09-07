package com.yolt.providers.belfius.belfius;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.ConsentBehavior.CONSENT_PER_ACCOUNT;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.BE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BelfiusDetailsProvider implements AisDetailsProvider {

    public static final String BELFIUS_SITE_ID = "61715853-e14c-46f3-b4ce-fdfe24cac95e";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS =List.of(
            site(BELFIUS_SITE_ID, "Belfius", "BELFIUS", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(BE))
                    .groupingBy("Belfius")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .consentBehavior(Set.of(CONSENT_PER_ACCOUNT))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}