package com.yolt.providers.openbanking.ais.lloydsbankinggroup.lloydscorporate.beanconfig;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.XF;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class LloydsCorpoDetailsProvider implements AisDetailsProvider {

    public static final String LLOYDS_CORPO_SITE_ID = "3d63abc7-499d-4b9a-8cfe-8fffb0341fb9";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(LLOYDS_CORPO_SITE_ID, "Lloyds Bank Corporate", "LLOYDS_BANK_CORPO", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT), of(XF))
                    .groupingBy("Lloyds Bank Corporate")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
