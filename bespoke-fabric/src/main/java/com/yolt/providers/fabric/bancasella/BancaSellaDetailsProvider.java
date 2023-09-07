package com.yolt.providers.fabric.bancasella;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.IT;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.NL;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BancaSellaDetailsProvider implements AisDetailsProvider {

    public static final String PROVIDER_SITE_ID = "e184dd3f-cd11-4ffc-945a-b178dc986bda";
    public static final String PROVIDER_IDENTIFIER = "BANCA_SELLA";
    public static final String PROVIDER_DISPLAY_NAME = "Banca Sella";
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(PROVIDER_SITE_ID, PROVIDER_DISPLAY_NAME, PROVIDER_IDENTIFIER, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, SAVINGS_ACCOUNT), of(IT))
                    .groupingBy(PROVIDER_DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
