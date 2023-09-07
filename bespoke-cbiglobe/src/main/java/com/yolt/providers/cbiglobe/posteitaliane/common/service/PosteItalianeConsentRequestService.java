package com.yolt.providers.cbiglobe.posteitaliane.common.service;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeConsentRequestServiceV4;
import com.yolt.providers.cbiglobe.common.service.ConsentAccessCreator;
import com.yolt.providers.cbiglobe.dto.EstablishConsentResponseTypeScaMethods;

import java.time.Clock;
import java.util.List;

public class PosteItalianeConsentRequestService extends CbiGlobeConsentRequestServiceV4 {

    public static final String ONLY_SUPPORTED_SCA_METHOD_ID = "1";

    public PosteItalianeConsentRequestService(CbiGlobeBaseProperties properties, Clock clock, ConsentAccessCreator consentAccessCreator) {
        super(properties, clock, consentAccessCreator);
    }

    @Override
    protected String getScaMethodId(List<EstablishConsentResponseTypeScaMethods> scaMethods) {
        return ONLY_SUPPORTED_SCA_METHOD_ID;
    }
}
