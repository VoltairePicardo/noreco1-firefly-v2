package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private BigDecimal amount;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_budgetId", nullable = true, columnDefinition = "0")
    private Budget budget;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_cashflowItemId", nullable = true, columnDefinition = "0")
    private CashflowItem cashflowItem;

    @Column(insertable = false, updatable = false)
    private BigDecimal amtOgm;

    @Column(insertable = false, updatable = false)
    private BigDecimal amtAod;

    @Column(insertable = false, updatable = false)
    private BigDecimal amtIsd;

    @Column(insertable = false, updatable = false)
    private BigDecimal amtFsd;

    @Column(insertable = false, updatable = false)
    private BigDecimal amtTsd;

    @Column(insertable = false, updatable = false)
    private BigDecimal amtBod;

    @Column(insertable = false, updatable = false)
    private BigDecimal totalYearBudget;

    @Transient
    private BigDecimal totalBalance;

    @Transient
    private BigDecimal totalAmount;

}
