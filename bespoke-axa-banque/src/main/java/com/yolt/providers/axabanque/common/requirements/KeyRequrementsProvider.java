package com.yolt.providers.axabanque.common.requirements;

import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Optional;

public interface KeyRequrementsProvider {
    Optional<KeyRequirements> getRequirements();
}
