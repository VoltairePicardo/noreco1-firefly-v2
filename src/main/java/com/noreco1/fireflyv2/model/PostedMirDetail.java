package com.noreco1.fireflyv2.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PostedMirDetail {

    private String particulars;
    private String account;
    private BigDecimal debit;
    private BigDecimal credit;

}
