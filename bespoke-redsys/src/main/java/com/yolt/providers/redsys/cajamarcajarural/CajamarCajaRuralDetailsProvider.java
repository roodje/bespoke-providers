package com.yolt.providers.redsys.cajamarcajarural;

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
public class CajamarCajaRuralDetailsProvider implements AisDetailsProvider {

    public static final String CAJAMAR_CAJA_RURAL_ES_SITE_ID = "4da888ad-df8b-4ed2-b9ea-c68553b69223";
    public static final String CAJAMAR_CAJA_RURAL_PROVIDER_DISPLAY_NAME = "Cajamar Caja Rural";
    public static final String CAJAMAR_CAJA_RURAL_PROVIDER_KEY = "CAJAMAR_CAJA_RURAL";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(CAJAMAR_CAJA_RURAL_ES_SITE_ID, CAJAMAR_CAJA_RURAL_PROVIDER_DISPLAY_NAME, CAJAMAR_CAJA_RURAL_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(ES))
                    .groupingBy(CAJAMAR_CAJA_RURAL_PROVIDER_DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
