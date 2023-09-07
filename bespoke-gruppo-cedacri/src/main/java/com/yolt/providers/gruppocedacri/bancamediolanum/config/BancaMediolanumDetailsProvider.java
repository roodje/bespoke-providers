package com.yolt.providers.gruppocedacri.bancamediolanum.config;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.IT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BancaMediolanumDetailsProvider implements AisDetailsProvider {

    public static final String BANCA_MEDIOLANUM_SITE_ID = "400c472c-a4ce-4d2f-be3d-59eb26291565";
    public static final String PROVIDER_KEY = "BANCA_MEDIOLANUM";
    public static final String DISPLAY_NAME = "Banca Mediolanum";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(BANCA_MEDIOLANUM_SITE_ID, DISPLAY_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(IT))
                    .groupingBy(DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
