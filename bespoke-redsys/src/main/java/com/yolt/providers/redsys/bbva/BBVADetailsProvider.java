package com.yolt.providers.redsys.bbva;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.*;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BBVADetailsProvider implements AisDetailsProvider {

    public static final String BBVA_ES_SITE_ID = "26c67b2b-91d3-491f-8121-b02fc9420101";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(BBVA_ES_SITE_ID, "BBVA", "BBVA", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(ES, FR, GB, BE, IT))
                    .groupingBy("BBVA")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
