package com.yolt.providers.axabanque.common.traceid;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;

import java.util.function.Supplier;

public class TraceIdProducer implements Supplier<String> {

    @Override
    public String get() {
        return ExternalTracingUtil.createLastExternalTraceId();
    }
}
