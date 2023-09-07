package com.yolt.providers.stet.cmarkeagroup.cmdusudoest;

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
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class CreditMutuelDuSudOuestProvider implements AisDetailsProvider {

    public static final String CREDIT_MUTUEL_DU_SUD_OUEST = "8d45b222-0b89-43da-b61c-e1e87e6530fc";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(CREDIT_MUTUEL_DU_SUD_OUEST, "Crédit Mutuel du Sud-Ouest", "CREDIT_MUTUEL_DU_SUD_OUEST", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(FR))
                    .groupingBy("Crédit Mutuel du Sud-Ouest")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
