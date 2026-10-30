package com.noreco1.fireflyv2.controller.response.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class PODetail {
    private String description;
    private Integer id;
    private String unitCode;
    private String brand;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;

    private BigDecimal vat;
    private BigDecimal discount;
}
