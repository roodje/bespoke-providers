package com.yolt.providers.stet.generic.auth.keyrequirements;

import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

public interface KeyRequirementsProducer {

    KeyRequirements produce(String keyIdName, String certificateName);
}
