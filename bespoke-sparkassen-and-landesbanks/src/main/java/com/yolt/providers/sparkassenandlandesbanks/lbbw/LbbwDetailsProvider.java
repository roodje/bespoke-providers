package com.yolt.providers.sparkassenandlandesbanks.lbbw;

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
public class LbbwDetailsProvider implements AisDetailsProvider {

    private static final String LBBW_SITE_ID = "b096d0fe-9e44-432b-84d2-93cd294eb3d5";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(LBBW_SITE_ID, "Landesbank Baden-Württemberg", "LBBW", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(DE))
                    .groupingBy("Landesbank Baden-Württemberg")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}