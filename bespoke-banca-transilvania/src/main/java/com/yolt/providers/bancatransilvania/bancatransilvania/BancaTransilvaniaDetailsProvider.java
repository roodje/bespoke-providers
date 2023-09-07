package com.yolt.providers.bancatransilvania.bancatransilvania;

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
public class BancaTransilvaniaDetailsProvider implements AisDetailsProvider {

    public static final String BANCA_TRANSILVANIA_SITE_ID = "19d89e99-a797-45ba-a331-d886ee1a086d";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(BANCA_TRANSILVANIA_SITE_ID, "Banca Transilvania", "BANCA_TRANSILVANIA", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(RO))
                    .groupingBy("Banca Transilvania")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }
}
