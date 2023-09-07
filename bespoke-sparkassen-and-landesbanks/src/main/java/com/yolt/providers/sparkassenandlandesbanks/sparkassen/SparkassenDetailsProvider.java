package com.yolt.providers.sparkassenandlandesbanks.sparkassen;

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
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class SparkassenDetailsProvider implements AisDetailsProvider {

    private static final String SPARKASSEN_SITE_ID = "8ac70240-e629-45d1-8dec-b646c34d8f6c";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(SPARKASSEN_SITE_ID, "Sparkassen", "SPARKASSEN", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(DE))
                    .groupingBy("Sparkassen")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}