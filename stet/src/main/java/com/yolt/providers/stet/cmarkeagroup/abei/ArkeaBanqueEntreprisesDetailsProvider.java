package com.yolt.providers.stet.cmarkeagroup.abei;

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
public class ArkeaBanqueEntreprisesDetailsProvider implements AisDetailsProvider {

    public static final String ARKEA_BANQUE_ENTREPRISES_SITE_ID = "d26b95c6-da25-4f49-8af6-942b058d4e2b";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(ARKEA_BANQUE_ENTREPRISES_SITE_ID, "Arkéa Banque Entreprises et Institutionnels", "ARKEA_BANQUE_ENTREPRISES", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(FR))
                    .groupingBy("Arkéa Banque Entreprises et Institutionnels")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
