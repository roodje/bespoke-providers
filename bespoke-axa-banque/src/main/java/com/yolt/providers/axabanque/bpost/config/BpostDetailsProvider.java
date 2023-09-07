package com.yolt.providers.axabanque.bpost.config;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.BE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BpostDetailsProvider implements AisDetailsProvider {

    public static final String BPOST_SITE_ID = "cacf9ba4-203f-11ec-9621-0242ac130002";

    public static final String PROVIDER_DISPLAY_NAME = "bpost";
    public static final String PROVIDER_KEY = "BPOST";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(BPOST_SITE_ID, PROVIDER_DISPLAY_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(BE))
                    .groupingBy(PROVIDER_DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
