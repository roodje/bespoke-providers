package com.yolt.providers.openbanking.ais.rbsgroup.natwestcorporate.beanconfig;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.GB;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class NatwestCorpoAisDetailsProvider implements AisDetailsProvider {

    private static final String SITE_ID = "94bd7d3c-a4d5-4b64-815e-00082b172630";
    private static final String PROVIDER_NAME = "NatWest Corporate";
    private static final String PROVIDER_KEY = "NATWEST_CORPO";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT), of(GB))
                    .groupingBy(PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }
}
