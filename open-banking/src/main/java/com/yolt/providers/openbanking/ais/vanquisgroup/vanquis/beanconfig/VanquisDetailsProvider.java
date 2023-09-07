package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.beanconfig;

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
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class VanquisDetailsProvider implements AisDetailsProvider {

    private static final String VANQUIS_SITE_ID = "e43c5a0d-70c2-45ca-91d2-98a18e1d8a02";
    private static final String PROVIDER_NAME = "Vanquis Bank (UK)";
    private static final String PROVIDER_KEY = "VANQUIS_BANK";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(VANQUIS_SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CREDIT_CARD), of(GB))
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
