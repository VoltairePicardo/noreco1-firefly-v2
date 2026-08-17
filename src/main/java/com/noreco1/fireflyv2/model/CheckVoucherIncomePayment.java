package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CheckVoucherIncomePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_transactionId")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_taxCodeId")
    private TaxCode taxCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_accountId")
    private Account account;

    @Column
    private BigDecimal amount = BigDecimal.ZERO;

    @Column
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column
    private BigDecimal percentage = BigDecimal.ZERO;

}
