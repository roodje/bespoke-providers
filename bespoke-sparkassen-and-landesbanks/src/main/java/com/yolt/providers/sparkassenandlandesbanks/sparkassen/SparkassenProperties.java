package com.yolt.providers.sparkassenandlandesbanks.sparkassen;

import com.yolt.providers.sparkassenandlandesbanks.common.Department;
import com.yolt.providers.sparkassenandlandesbanks.common.config.SparkassenAndLandesbanksProperties;
import lombok.Data;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Size;
import java.util.List;

@Component
@ConfigurationProperties("lovebird.sparkassen")
@Data
public class SparkassenProperties extends SparkassenAndLandesbanksProperties {

    @Size(min = 1)
    private List<Department> departments;

    public Department getSelectedDepartment(FilledInUserSiteFormValues form) {
        for (Department data : getDepartments()) {
            if (data.getFormValue().equals(form.get("bank"))) {
                return data;
            }
        }
        throw new IllegalStateException("Department does not exist");
    }
}
