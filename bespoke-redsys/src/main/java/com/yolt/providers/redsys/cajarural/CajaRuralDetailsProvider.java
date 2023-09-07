package com.yolt.providers.redsys.cajarural;

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
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class CajaRuralDetailsProvider implements AisDetailsProvider {

    public static final String CAJA_RURAL_ES_SITE_ID = "33f0fc98-d825-4d8b-952a-e4fe483f11f4";
    public static final String CAJA_RURAL_PROVIDER_DISPLAY_NAME = "Caja Rural";
    public static final String CAJA_RURAL_PROVIDER_KEY = "CAJA_RURAL";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(CAJA_RURAL_ES_SITE_ID, CAJA_RURAL_PROVIDER_DISPLAY_NAME, CAJA_RURAL_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(ES))
                    .groupingBy(CAJA_RURAL_PROVIDER_DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
