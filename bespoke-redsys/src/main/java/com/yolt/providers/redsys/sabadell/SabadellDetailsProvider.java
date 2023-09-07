package com.yolt.providers.redsys.sabadell;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.ES;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class SabadellDetailsProvider implements AisDetailsProvider {

    public static final String SABADELL_SITE_ID = "49665f18-2cc6-4b0a-bd66-c8037dda7058";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(SABADELL_SITE_ID, "Sabadell", "SABADELL", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(ES))
                    .groupingBy("Sabadell")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
