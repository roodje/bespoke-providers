package com.yolt.providers.direkt1822group.direkt1822.config;

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
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class Direkt1822AisDetailsProvider implements AisDetailsProvider {

    public static final String DIRECT1822_SITE_ID = "83c792c0-f7f7-4714-b763-1a6522918228";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(DIRECT1822_SITE_ID, "1822Direkt", "DIREKT1822", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, SAVINGS_ACCOUNT), of(DE))
                    .groupingBy("1822Direkt")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
