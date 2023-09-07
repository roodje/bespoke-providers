package com.yolt.providers.dkbgroup.dkb;

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
public class DKBDetailsProvider implements AisDetailsProvider {

    public static final String DKB_SITE_ID = "cc6b36d7-e989-428c-ac1c-da5a51aa6d59";
    public static final String DKB_PROVIDER_KEY = "DKB";
    public static final String DKB_DISPLAY_NAME = "Deutsche Kreditbank Berlin";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(DKB_SITE_ID, DKB_DISPLAY_NAME, DKB_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(DE))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.FORM))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
