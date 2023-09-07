package com.yolt.providers.abancagroup.abanca.config;

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
public class AbancaDetailsProvider implements AisDetailsProvider {

    public static final String IDENTIFIER = "ABANCA";
    public static final String DISPLAY_NAME = "Abanca";
    public static final String ABANCA_SITE_ID = "9089bf43-174f-48c8-b50f-674a502f4db5";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(
                    ABANCA_SITE_ID,
                    DISPLAY_NAME,
                    IDENTIFIER,
                    DIRECT_CONNECTION,
                    of(STATE),
                    of(CURRENT_ACCOUNT), //CURRENT, CREDIT and SAVINGS are supported but there is no way to distinguish them.
                    of(ES))
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

