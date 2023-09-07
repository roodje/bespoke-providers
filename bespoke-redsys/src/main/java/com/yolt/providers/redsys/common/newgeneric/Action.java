package com.yolt.providers.redsys.common.newgeneric;

import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;

public interface Action<T extends SerializableConsentProcessData> {
    AccessMeansOrStepDTO run(ConsentProcessArguments<T> processArguments);
}
