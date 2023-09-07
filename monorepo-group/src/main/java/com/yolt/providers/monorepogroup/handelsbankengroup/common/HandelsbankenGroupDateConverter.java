package com.yolt.providers.monorepogroup.handelsbankengroup.common;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@RequiredArgsConstructor
public class HandelsbankenGroupDateConverter {

    private final ZoneId zoneId;

    public Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }
}
