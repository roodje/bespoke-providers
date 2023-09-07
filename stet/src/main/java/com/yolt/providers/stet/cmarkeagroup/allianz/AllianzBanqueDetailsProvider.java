package com.yolt.providers.stet.cmarkeagroup.allianz;

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
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class AllianzBanqueDetailsProvider implements AisDetailsProvider {

    public static final String ALLIANZ_BANQUE_SITE_ID = "0ac9244f-811b-446c-b7c1-f46473887908";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(ALLIANZ_BANQUE_SITE_ID, "Allianz Banque", "ALLIANZ_BANQUE", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(FR))
                    .groupingBy("Allianz Banque")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
