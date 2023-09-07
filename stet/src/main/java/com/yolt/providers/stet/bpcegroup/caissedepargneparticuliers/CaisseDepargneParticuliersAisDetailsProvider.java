package com.yolt.providers.stet.bpcegroup.caissedepargneparticuliers;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.FR;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class CaisseDepargneParticuliersAisDetailsProvider implements AisDetailsProvider {

    public static final String CAISSE_DEPARGNE_PARTICULARIES_SITE_ID = "ccd64612-a172-11e9-a2a3-2a2ae2dbcce4";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(CAISSE_DEPARGNE_PARTICULARIES_SITE_ID, "Caisse d'Épargne Particuliers", "CAISSE_DEPARGNE_PARTICULIERS", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR))
                    .groupingBy("Caisse d'Épargne Particuliers")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
