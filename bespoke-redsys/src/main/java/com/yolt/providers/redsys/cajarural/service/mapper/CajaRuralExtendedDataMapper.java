package com.yolt.providers.redsys.cajarural.service.mapper;

import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.redsys.common.service.mapper.RedsysExtendedDataMapperV2;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CajaRuralExtendedDataMapper extends RedsysExtendedDataMapperV2 {

    private final ZoneId zoneId;

    public CajaRuralExtendedDataMapper(final CurrencyCodeMapper currencyCodeMapper, final ZoneId zoneId) {
        super(currencyCodeMapper, zoneId);
        this.zoneId = zoneId;
    }

    @Override
    protected ZonedDateTime toLastChangeDateTime(final String lastChangeDateTime) {
        return StringUtils.isEmpty(lastChangeDateTime)
                ? null : LocalDateTime.parse(lastChangeDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(zoneId);
    }

}
