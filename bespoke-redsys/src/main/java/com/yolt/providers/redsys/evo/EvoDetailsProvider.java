package com.yolt.providers.redsys.evo;

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
public class EvoDetailsProvider implements AisDetailsProvider {

    public static final String EVO_BANCO_SITE_ID = "a5e54d0a-9756-4365-aea8-928bd3ad5876";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(EVO_BANCO_SITE_ID, "Evo Banco", "EVO", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(ES))
                    .groupingBy("Evo Banco")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
