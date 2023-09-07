package com.yolt.providers.sparkassenandlandesbanks.nordlb;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.DE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class NordLbDetailsProvider implements AisDetailsProvider {

    private static final String NORD_LB_SITE_ID = "339d9c63-2d7d-4572-a20c-377eb30556b0";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(NORD_LB_SITE_ID, "Norddeutsche Landesbank", "NORD_LB", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(DE))
                    .groupingBy("Norddeutsche Landesbank")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}