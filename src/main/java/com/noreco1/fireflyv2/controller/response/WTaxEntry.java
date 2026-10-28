package com.noreco1.fireflyv2.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.model.TaxCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WTaxEntry {

    private Map<String, Object> account;
    private BigDecimal percentage;
    private BigDecimal baseAmount;
    private BigDecimal amount;
    private TaxCode atc;

}
