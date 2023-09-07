package com.yolt.providers.openbanking.ais.hsbcgroup.hsbccorpo.beanconfig;

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
public class HsbcCorpoDetailsProvider implements AisDetailsProvider {

    public static final String HSBC_CORPO_SITE_ID = "fdf5c571-7025-4569-b240-2be87b22f9aa";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(HSBC_CORPO_SITE_ID, "HSBC Corporate", "HSBC_CORPO", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, SAVINGS_ACCOUNT, CREDIT_CARD), of(GB))
                    .groupingBy("HSBC Corporate")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
