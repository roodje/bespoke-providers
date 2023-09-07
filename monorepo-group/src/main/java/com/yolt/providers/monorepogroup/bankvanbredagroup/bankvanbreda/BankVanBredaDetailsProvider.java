package com.yolt.providers.monorepogroup.bankvanbredagroup.bankvanbreda;

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
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BankVanBredaDetailsProvider implements AisDetailsProvider {

    public static final String BANK_VAN_BREDA_SITE_ID = "70eb88bd-7fae-4bff-b80b-88099538f292";
    public static final String BANK_VAN_BREDA_PROVIDER_KEY = "BANK_VAN_BREDA";
    public static final String BANK_VAN_BREDA_PROVIDER_NAME = "Bank Van Breda";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(BANK_VAN_BREDA_SITE_ID, BANK_VAN_BREDA_PROVIDER_NAME, BANK_VAN_BREDA_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(BE))
                    .groupingBy(BANK_VAN_BREDA_PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
