package com.yolt.providers.belfius.belfius.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.belfius.common.service.BelfiusGroupAuthorizationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BelfiusAuthorizationService extends BelfiusGroupAuthorizationService {
    public BelfiusAuthorizationService(@Qualifier("BelfiusObjectMapper") ObjectMapper mapper) {
        super(mapper);
    }
}