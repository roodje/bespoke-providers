package com.yolt.providers.openbanking.ais.barclaysgroup.barclays.config;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties("lovebird.barclaysgroup.barclays")
@Data
@EqualsAndHashCode(callSuper = true)
@Validated
public class BarclaysPropertiesV3 extends DefaultProperties {

    @NotNull
    private List<CustomerType> customerTypes;

    public CustomerType getCustomerTypeByCode(String code) {
        return customerTypes.stream()
                .filter(customerType -> customerType.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Customer type %s does not exist", code)));
    }
}
