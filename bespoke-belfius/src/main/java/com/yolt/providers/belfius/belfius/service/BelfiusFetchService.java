package com.yolt.providers.belfius.belfius.service;


import com.yolt.providers.belfius.common.service.BelfiusGroupFetchDataService;
import com.yolt.providers.belfius.common.service.mapper.BelfiusGroupMapper;
import org.springframework.stereotype.Service;

@Service
public class BelfiusFetchService extends BelfiusGroupFetchDataService {

    public BelfiusFetchService(BelfiusGroupMapper mapper) {
        super(mapper);
    }
}
