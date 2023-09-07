package com.yolt.providers.redsys.caixa;

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
public class CaixaDetailsProvider implements AisDetailsProvider {

    public static final String CAIXA_ES_SITE_ID = "9811a3bd-1c8d-4461-b795-4ff63868fc58";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(CAIXA_ES_SITE_ID, "La Caixa", "CAIXA", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(ES))
                    .groupingBy("La Caixa")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
