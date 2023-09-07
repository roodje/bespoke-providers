package com.yolt.providers.n26.n26.config;

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
public class N26DetailsProvider implements AisDetailsProvider {

    public static final String N26_SITE_ID = "a6fe9812-a242-4902-850d-20b29102dba2";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(N26_SITE_ID, "N26", "N26", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR, IT, ES, BE, DE, NL, IE, AT, CZ, RO, PL, LU))
                    .groupingBy("N26")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
