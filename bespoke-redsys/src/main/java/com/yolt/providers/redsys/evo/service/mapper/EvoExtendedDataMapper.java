package com.yolt.providers.redsys.evo.service.mapper;

import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.redsys.common.service.mapper.RedsysExtendedDataMapperV2;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class EvoExtendedDataMapper extends RedsysExtendedDataMapperV2 {

    private final ZoneId zoneId;

    public EvoExtendedDataMapper(final CurrencyCodeMapper currencyCodeMapper, final ZoneId zoneId) {
        super(currencyCodeMapper, zoneId);
        this.zoneId = zoneId;
    }

    @Override
    protected ZonedDateTime toLastChangeDateTime(final String lastChangeDateTime) {
        return StringUtils.isEmpty(lastChangeDateTime)
                ? null : ZonedDateTime.parse(lastChangeDateTime).withZoneSameLocal(zoneId);
    }

}
