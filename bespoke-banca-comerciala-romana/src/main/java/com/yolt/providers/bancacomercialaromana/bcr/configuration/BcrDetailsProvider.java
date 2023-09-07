package com.yolt.providers.bancacomercialaromana.bcr.configuration;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.RO;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BcrDetailsProvider implements AisDetailsProvider {

    public static final String BCR_SITE_ID = "9c4b62f9-604f-478d-830f-37436585d91f";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(BCR_SITE_ID, "Banca Comerciala Romana", "BANCA_COMERCIALA_ROMANA", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(RO))
                    .groupingBy("Banca Comerciala Romana")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }
}
