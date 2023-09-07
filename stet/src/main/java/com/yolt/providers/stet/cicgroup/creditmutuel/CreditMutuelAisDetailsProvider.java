package com.yolt.providers.stet.cicgroup.creditmutuel;

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
public class CreditMutuelAisDetailsProvider implements AisDetailsProvider {

    public static final String CREDIT_MUTUEL_SITE_ID = "8194a783-cb30-49d5-923a-d814479d6386";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(CREDIT_MUTUEL_SITE_ID, "Crédit Mutuel", "CREDIT_MUTUEL", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(FR))
                    .groupingBy("Crédit Mutuel")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
