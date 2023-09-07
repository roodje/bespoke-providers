package com.yolt.providers.openbanking.ais.sainsburys.beanconfig;

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
public class SainsburysDetailsProvider implements AisDetailsProvider {

    private static final String SAINSBURYS_SITE_ID = "2561b5e4-fa27-4d1d-ad3d-6085ffbd7aca";
    private static final String PROVIDER_NAME = "Sainsbury's Bank";
    private static final String PROVIDER_KEY = "SAINSBURYS_BANK";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(SAINSBURYS_SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CREDIT_CARD), of(GB))
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
