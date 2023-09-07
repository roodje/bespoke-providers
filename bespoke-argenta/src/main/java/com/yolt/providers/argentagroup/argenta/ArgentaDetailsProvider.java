package com.yolt.providers.argentagroup.argenta;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.BE;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class ArgentaDetailsProvider implements AisDetailsProvider {

    public static final String ARGENTA_SITE_ID = "d51cb308-e0ac-11eb-ba80-0242ac130004";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(ARGENTA_SITE_ID, "Argenta", "ARGENTA", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, SAVINGS_ACCOUNT), of(BE))
                    .groupingBy("Argenta")
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
