package com.yolt.providers.nutmeggroup.nutmeg.configuration;

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
import static nl.ing.lovebird.providerdomain.AccountType.PENSION;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class NutmegDetailsProvider implements AisDetailsProvider {

    public static final String NUTMEG_SITE_ID = "434a98d7-29b1-4c98-b75a-ce7652b1d741";
    private static final String PROVIDER_NAME = "Nutmeg";
    private static final String PROVIDER_KEY = "NUTMEG";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(NUTMEG_SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(PENSION), of(GB))
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
