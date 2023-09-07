package com.yolt.providers.redsys.santander;

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
public class SantanderEsDetailsProvider implements AisDetailsProvider {

    public static final String SANTANDER_ES_SITE_ID = "bb5c3c49-f6c2-45c2-9067-855df78e6864";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(SANTANDER_ES_SITE_ID, "Santander (ES)", "SANTANDER_ES", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(ES))
                    .groupingBy("Santander (ES)")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
