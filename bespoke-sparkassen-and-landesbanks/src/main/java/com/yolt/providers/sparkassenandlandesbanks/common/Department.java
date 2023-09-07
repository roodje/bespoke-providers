package com.yolt.providers.sparkassenandlandesbanks.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Department {

    private String formValue;
    private String displayName;
    private String bankCode;

    public Department(String bankCode) {
        this.bankCode = bankCode;
    }
}
