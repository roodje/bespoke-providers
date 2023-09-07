package com.yolt.providers.stet.cmarkeagroup.fortuneo;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import nl.ing.lovebird.providerdomain.ServiceType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.CountryCode.FR;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;

@Service
public class FortuneoAisDetailsProvider implements AisDetailsProvider {

    public static final String FORTUNEO_SITE_ID = "c3238eb9-838a-432a-a68f-5acf8e80de03";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(AisSiteDetails.site(FORTUNEO_SITE_ID, "Fortuneo Banque",
                    "FORTUNEO", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(FR))
            .groupingBy("Fortuneo Banque")
            .usesStepTypes(Map.of(ServiceType.AIS, of(LoginRequirement.REDIRECT)))
            .loginRequirements(of(LoginRequirement.REDIRECT))
            .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }
}
