package com.yolt.providers.monorepogroup.olbgroup.olb;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.DE;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.FORM;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.REDIRECT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class OlbDetailsProvider implements AisDetailsProvider {

    public static final String OLB_SITE_ID = "e31abee4-f3b3-11ec-b939-0242ac120002";
    public static final String OLB_PROVIDER_KEY = "OLB_BANK";
    public static final String OLB_PROVIDER_NAME = "Oldenburgische Landesbank";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(OLB_SITE_ID, OLB_PROVIDER_NAME, OLB_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(DE))
                    .groupingBy(OLB_PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(REDIRECT, FORM)))
                    .loginRequirements(of(REDIRECT, FORM))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
