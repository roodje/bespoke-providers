package com.yolt.providers.openbanking.ais.lloydsbankinggroup.bankofscotlandcorpo.beanconfig;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.XF;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class BankOfScotlandCorpoDetailsProvider implements AisDetailsProvider {

    public static final String BANK_OF_SCOTLAND_SITE_ID = "a42ca6bf-872c-4b2c-b2bb-860619866ae9";
    public static final String PROVIDER_NAME = "Bank of Scotland Corporate";
    public static final String PROVIDER_KEY = "BANK_OF_SCOTLAND_CORPO";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(BANK_OF_SCOTLAND_SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT), of(XF))
                    .groupingBy(PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }
}
